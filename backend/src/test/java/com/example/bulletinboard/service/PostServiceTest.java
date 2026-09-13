package com.example.bulletinboard.service;

import com.example.bulletinboard.dto.CreatePostRequest;
import com.example.bulletinboard.dto.PagedPostResponse;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.entity.PostEntity;
import com.example.bulletinboard.repository.PostRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for PostService verifying domain validation, pagination, and persistence contracts.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("掲示板投稿ドメインサービスの単体テスト")
class PostServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-11T10:15:30Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    private static final LocalDateTime EXPECTED_NOW = LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC);

    @Mock
    PostRepository postRepository;

    PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, FIXED_CLOCK);
    }

    @Test
    @DisplayName("正常な投稿リクエストの場合、文字列をトリムしUTC日時を付与して永続化し、PostResponseを返却すること")
    void testCreatePostSuccessfully() {
        CreatePostRequest request = new CreatePostRequest(
                "  Alice  ",
                " alice@example.com ",
                "  Greeting  ",
                "  Hello World!  "
        );

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity entity = invocation.getArgument(0);
            return new PostEntity(
                    100L,
                    entity.name(),
                    entity.email(),
                    entity.title(),
                    entity.message(),
                    entity.createdAt()
            );
        });

        PostResponse response = postService.createPost(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Alice", response.name());
        assertEquals("alice@example.com", response.email());
        assertEquals("Greeting", response.title());
        assertEquals("Hello World!", response.message());
        assertEquals("2026-09-11T10:15:30Z", response.createdAt());

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        PostEntity saved = captor.getValue();
        assertEquals("Alice", saved.name());
        assertEquals("alice@example.com", saved.email());
        assertEquals("Greeting", saved.title());
        assertEquals("Hello World!", saved.message());
        assertEquals(EXPECTED_NOW, saved.createdAt());
    }

    @ParameterizedTest(name = "空白メールアドレス: ''{0}'' が null に正規化されること")
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName("空文字または空白のみのメールアドレスが渡された場合、nullに正規化して保存すること")
    void testCreatePostNormalizesBlankEmailToNull(String blankEmail) {
        CreatePostRequest request = new CreatePostRequest(
                "Bob",
                blankEmail,
                "Title",
                "Message"
        );

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity entity = invocation.getArgument(0);
            return new PostEntity(101L, entity.name(), entity.email(), entity.title(), entity.message(), entity.createdAt());
        });

        PostResponse response = postService.createPost(request);

        assertNull(response.email(), "空白メールはnullに正規化されなければならない");

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        assertNull(captor.getValue().email());
    }

    @Test
    @DisplayName("メールアドレスがnullの場合、nullのまま安全に保存されること")
    void testCreatePostWithNullEmail() {
        CreatePostRequest request = new CreatePostRequest(
                "Charlie",
                null,
                "Title",
                "Message"
        );

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity entity = invocation.getArgument(0);
            return new PostEntity(102L, entity.name(), entity.email(), entity.title(), entity.message(), entity.createdAt());
        });

        PostResponse response = postService.createPost(request);

        assertNull(response.email());

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        assertNull(captor.getValue().email(), "リポジトリに渡されるエンティティのメールもnullでなければならない");
    }

    @Test
    @DisplayName("事前条件違反: リクエストオブジェクトがnullの場合、IllegalArgumentExceptionが発生すること")
    void testCreatePostRejectsNullCommand() {
        assertThrows(IllegalArgumentException.class, () -> postService.createPost(null),
                "nullのリクエストは拒否されなければならない");
        verify(postRepository, never()).save(any());
    }

    @ParameterizedTest(name = "不正な必須フィールド（空白・空文字・null）: name=''{0}'', title=''{1}'', message=''{2}''")
    @CsvSource(value = {
            "'   ', 'Valid Title', 'Valid Message'",
            "'', 'Valid Title', 'Valid Message'",
            "NIL, 'Valid Title', 'Valid Message'",
            "'Valid Name', '   ', 'Valid Message'",
            "'Valid Name', '', 'Valid Message'",
            "'Valid Name', NIL, 'Valid Message'",
            "'Valid Name', 'Valid Title', '   '",
            "'Valid Name', 'Valid Title', ''",
            "'Valid Name', 'Valid Title', NIL"
    }, nullValues = {"NIL"})
    @DisplayName("事前条件違反: 必須フィールドが空白文字・空文字・nullの場合、IllegalArgumentExceptionが発生すること")
    void testCreatePostRejectsBlankFields(String name, String title, String message) {
        CreatePostRequest request = new CreatePostRequest(name, null, title, message);

        assertThrows(IllegalArgumentException.class, () -> postService.createPost(request),
                "空白・空文字・nullの必須項目は事前条件違反として拒否されなければならない");
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("正常なページネーションリクエストの場合、降順ソートでPageableを渡し、PagedPostResponseを返却すること")
    void testGetPagedPostsSuccessfully() {
        LocalDateTime now = LocalDateTime.now();
        List<PostEntity> entities = List.of(
                new PostEntity(2L, "User2", null, "Title2", "Message2", now),
                new PostEntity(1L, "User1", null, "Title1", "Message1", now.minusMinutes(1))
        );

        Page<PostEntity> mockPage = Page.of(entities, Pageable.from(0, 50), 2L);
        when(postRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        PagedPostResponse response = postService.getPagedPosts(0, 50);

        assertNotNull(response);
        assertEquals(2, response.items().size());
        assertEquals(0, response.page());
        assertEquals(50, response.size());
        assertEquals(2L, response.totalItems());
        assertEquals(1, response.totalPages());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findAll(captor.capture());
        Pageable passed = captor.getValue();
        assertEquals(0, passed.getNumber());
        assertEquals(50, passed.getSize());
        assertTrue(passed.getSort().isSorted());
        Sort.Order order = passed.getSort().getOrderBy().stream()
                .filter(o -> "createdAt".equals(o.getProperty()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Sort order on 'createdAt' is missing"));
        assertEquals(Sort.Order.Direction.DESC, order.getDirection(), "Sort direction must be DESC");
    }

    @Test
    @DisplayName("レコードが0件の場合、nullではなく空リスト [] を保証した PagedPostResponse を返却すること")
    void testGetPagedPostsEmptyGuaranteesEmptyList() {
        Page<PostEntity> emptyPage = Page.of(List.of(), Pageable.from(0, 50), 0L);
        when(postRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PagedPostResponse response = postService.getPagedPosts(0, 50);

        assertNotNull(response);
        assertNotNull(response.items(), "items は決してnullであってはならない");
        assertTrue(response.items().isEmpty(), "0件の場合は空リストでなければならない");
        assertEquals(0L, response.totalItems());
        assertEquals(0, response.totalPages());
    }

    @ParameterizedTest(name = "不正なページネーション引数: page={0}, size={1}")
    @CsvSource({
            "-1, 50",
            "0, 0",
            "0, -5",
            "0, 51",
            "0, 100"
    })
    @DisplayName("事前条件違反: 負のページ番号や範囲外（1〜50以外）のサイズが指定された場合、IllegalArgumentExceptionが発生すること")
    void testGetPagedPostsRejectsInvalidPagination(int page, int size) {
        assertThrows(IllegalArgumentException.class, () -> postService.getPagedPosts(page, size),
                "範囲外のページネーション引数は拒否されなければならない");
        verify(postRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("事前条件違反: nullの依存オブジェクトをコンストラクタに渡した場合、NullPointerExceptionが発生すること")
    void testConstructorRejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> new PostService(null));
        assertThrows(NullPointerException.class, () -> new PostService(postRepository, null));
    }

    @Test
    @DisplayName("デフォルトコンストラクタの初期化: システムUTCクロックで正常に初期化されること")
    void testDefaultConstructorInstantiates() {
        PostService defaultService = new PostService(postRepository);
        assertNotNull(defaultService);
    }
}
