package com.example.bulletinboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bulletinboard.dto.CreatePostRequest;
import com.example.bulletinboard.dto.CreateReplyRequest;
import com.example.bulletinboard.dto.PagedPostResponse;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.dto.ReplyResponse;
import com.example.bulletinboard.entity.PostEntity;
import com.example.bulletinboard.entity.ReplyEntity;
import com.example.bulletinboard.exception.PostNotFoundException;
import com.example.bulletinboard.repository.PostRepository;
import com.example.bulletinboard.repository.ReplyRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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

/**
 * Unit test suite for PostService verifying domain validation, pagination, and persistence contracts.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit test suite for PostService")
class PostServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-11T10:15:30Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    private static final LocalDateTime EXPECTED_NOW = LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC);

    @Mock
    PostRepository postRepository;

    @Mock
    ReplyRepository replyRepository;

    PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, replyRepository, FIXED_CLOCK);
    }

    @Test
    @DisplayName("Valid post request: should trim strings, set UTC timestamp, persist entity, and return PostResponse")
    void testCreatePostSuccessfully() {
        CreatePostRequest request =
                new CreatePostRequest("  Alice  ", " alice@example.com ", "  Greeting  ", "  Hello World!  ");

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity entity = invocation.getArgument(0);
            return new PostEntity(
                    100L, entity.name(), entity.email(), entity.title(), entity.message(), entity.createdAt());
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

    @ParameterizedTest(name = "blank email: ''{0}'' should normalize to null")
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName("Blank or whitespace-only email should normalize to null before persistence")
    void testCreatePostNormalizesBlankEmailToNull(String blankEmail) {
        CreatePostRequest request = new CreatePostRequest("Bob", blankEmail, "Title", "Message");

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity entity = invocation.getArgument(0);
            return new PostEntity(
                    101L, entity.name(), entity.email(), entity.title(), entity.message(), entity.createdAt());
        });

        PostResponse response = postService.createPost(request);

        assertNull(response.email(), "Blank email must be normalized to null");

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        assertNull(captor.getValue().email());
    }

    @Test
    @DisplayName("Null email: should safely persist null email")
    void testCreatePostWithNullEmail() {
        CreatePostRequest request = new CreatePostRequest("Charlie", null, "Title", "Message");

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity entity = invocation.getArgument(0);
            return new PostEntity(
                    102L, entity.name(), entity.email(), entity.title(), entity.message(), entity.createdAt());
        });

        PostResponse response = postService.createPost(request);

        assertNull(response.email());

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        assertNull(captor.getValue().email(), "Entity email passed to repository must also be null");
    }

    @Test
    @DisplayName("Precondition violation: null request object must throw IllegalArgumentException")
    void testCreatePostRejectsNullCommand() {
        assertThrows(
                IllegalArgumentException.class, () -> postService.createPost(null), "Null request must be rejected");
        verify(postRepository, never()).save(any());
    }

    @ParameterizedTest(name = "invalid required field (blank/empty/null): name=''{0}'', title=''{1}'', message=''{2}''")
    @CsvSource(
            value = {
                "'   ', 'Valid Title', 'Valid Message'",
                "'', 'Valid Title', 'Valid Message'",
                "NIL, 'Valid Title', 'Valid Message'",
                "'Valid Name', '   ', 'Valid Message'",
                "'Valid Name', '', 'Valid Message'",
                "'Valid Name', NIL, 'Valid Message'",
                "'Valid Name', 'Valid Title', '   '",
                "'Valid Name', 'Valid Title', ''",
                "'Valid Name', 'Valid Title', NIL"
            },
            nullValues = {"NIL"})
    @DisplayName("Precondition violation: blank, empty, or null required fields must throw IllegalArgumentException")
    void testCreatePostRejectsBlankFields(String name, String title, String message) {
        CreatePostRequest request = new CreatePostRequest(name, null, title, message);

        assertThrows(
                IllegalArgumentException.class,
                () -> postService.createPost(request),
                "Blank, empty, or null required fields must be rejected as precondition violation");
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Valid pagination request: should pass Pageable sorted descending and return PagedPostResponse")
    void testGetPagedPostsSuccessfully() {
        LocalDateTime now = LocalDateTime.now();
        List<PostEntity> entities = List.of(
                new PostEntity(2L, "User2", null, "Title2", "Message2", now),
                new PostEntity(1L, "User1", null, "Title1", "Message1", now.minusMinutes(1)));

        Page<PostEntity> mockPage = Page.of(entities, Pageable.from(0, 50), 2L);
        when(postRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        PagedPostResponse response = postService.getPagedPosts(0, 50);

        assertNotNull(response);
        assertEquals(2, response.items().size());
        assertEquals(0, response.page());
        assertEquals(50, response.size());
        assertEquals(2L, response.totalItems());
        assertEquals(1, response.totalPages());

        assertNotNull(response.items().get(0).replies());
        assertTrue(response.items().get(0).replies().isEmpty(), "Post 0 replies must default to empty list []");
        assertNotNull(response.items().get(1).replies());
        assertTrue(response.items().get(1).replies().isEmpty(), "Post 1 replies must default to empty list []");

        verify(replyRepository).findByPostIdInOrderByCreatedAtAsc(List.of(2L, 1L));

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
    @DisplayName(
            "Defensive contract: should guarantee empty list [] instead of null in PagedPostResponse when zero records exist")
    void testGetPagedPostsEmptyGuaranteesEmptyList() {
        Page<PostEntity> emptyPage = Page.of(List.of(), Pageable.from(0, 50), 0L);
        when(postRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PagedPostResponse response = postService.getPagedPosts(0, 50);

        assertNotNull(response);
        assertNotNull(response.items(), "items must never be null");
        assertTrue(response.items().isEmpty(), "items must be empty list when zero records exist");
        assertEquals(0L, response.totalItems());
        assertEquals(0, response.totalPages());
    }

    @ParameterizedTest(name = "invalid pagination arguments: page={0}, size={1}")
    @CsvSource({"-1, 50", "0, 0", "0, -5", "0, 51", "0, 100"})
    @DisplayName(
            "Precondition violation: negative page or out-of-range size (not 1-50) must throw IllegalArgumentException")
    void testGetPagedPostsRejectsInvalidPagination(int page, int size) {
        assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPagedPosts(page, size),
                "Out-of-range pagination arguments must be rejected");
        verify(postRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Precondition violation: passing null dependencies to constructor must throw NullPointerException")
    void testConstructorRejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> new PostService(null, replyRepository, FIXED_CLOCK));
        assertThrows(NullPointerException.class, () -> new PostService(postRepository, null, FIXED_CLOCK));
        assertThrows(NullPointerException.class, () -> new PostService(postRepository, replyRepository, null));
    }

    @Test
    @DisplayName("Default constructor: should instantiate successfully with system UTC clock")
    void testDefaultConstructorInstantiates() {
        PostService defaultService = new PostService(postRepository, replyRepository);
        assertNotNull(defaultService);
    }

    @Test
    @DisplayName("Valid reply request: should trim strings, verify parent post, save entity, and return ReplyResponse")
    void testCreateReplySuccessfully() {
        CreateReplyRequest request = new CreateReplyRequest("  Bob  ", " bob@example.com ", "  Nice post!  ");
        PostEntity parentPost = new PostEntity(10L, "Alice", null, "Title", "Message", EXPECTED_NOW);

        when(postRepository.findById(10L)).thenReturn(Optional.of(parentPost));
        when(replyRepository.save(any(ReplyEntity.class))).thenAnswer(invocation -> {
            ReplyEntity entity = invocation.getArgument(0);
            return new ReplyEntity(
                    50L, entity.postId(), entity.name(), entity.email(), entity.message(), entity.createdAt());
        });

        ReplyResponse response = postService.createReply(10L, request);

        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals(10L, response.postId());
        assertEquals("Bob", response.name());
        assertEquals("bob@example.com", response.email());
        assertEquals("Nice post!", response.message());
        assertEquals("2026-09-11T10:15:30Z", response.createdAt());

        ArgumentCaptor<ReplyEntity> captor = ArgumentCaptor.forClass(ReplyEntity.class);
        verify(replyRepository).save(captor.capture());
        ReplyEntity saved = captor.getValue();
        assertEquals(10L, saved.postId());
        assertEquals("Bob", saved.name());
        assertEquals("bob@example.com", saved.email());
        assertEquals("Nice post!", saved.message());
        assertEquals(EXPECTED_NOW, saved.createdAt());
    }

    @ParameterizedTest(name = "blank email: ''{0}'' should normalize to null")
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName(
            "Valid reply request with blank or whitespace-only email should normalize email to null in entity and DTO")
    void testCreateReplyNormalizesBlankEmailToNull(String blankEmail) {
        CreateReplyRequest request = new CreateReplyRequest("Bob", blankEmail, "Message");
        PostEntity parentPost = new PostEntity(10L, "Alice", null, "Title", "Message", EXPECTED_NOW);

        when(postRepository.findById(10L)).thenReturn(Optional.of(parentPost));
        when(replyRepository.save(any(ReplyEntity.class))).thenAnswer(invocation -> {
            ReplyEntity entity = invocation.getArgument(0);
            return new ReplyEntity(
                    51L, entity.postId(), entity.name(), entity.email(), entity.message(), entity.createdAt());
        });

        ReplyResponse response = postService.createReply(10L, request);

        assertNotNull(response);
        assertNull(response.email(), "Email in response DTO must be normalized to null when blank");

        ArgumentCaptor<ReplyEntity> captor = ArgumentCaptor.forClass(ReplyEntity.class);
        verify(replyRepository).save(captor.capture());
        assertNull(captor.getValue().email(), "Entity email passed to repository must also be null");
    }

    @Test
    @DisplayName("Valid reply request with null email should persist null email in entity and DTO")
    void testCreateReplyWithNullEmail() {
        CreateReplyRequest request = new CreateReplyRequest("Bob", null, "Message");
        PostEntity parentPost = new PostEntity(10L, "Alice", null, "Title", "Message", EXPECTED_NOW);

        when(postRepository.findById(10L)).thenReturn(Optional.of(parentPost));
        when(replyRepository.save(any(ReplyEntity.class))).thenAnswer(invocation -> {
            ReplyEntity entity = invocation.getArgument(0);
            return new ReplyEntity(
                    52L, entity.postId(), entity.name(), entity.email(), entity.message(), entity.createdAt());
        });

        ReplyResponse response = postService.createReply(10L, request);

        assertNotNull(response);
        assertNull(response.email(), "Email in response DTO must be null");

        ArgumentCaptor<ReplyEntity> captor = ArgumentCaptor.forClass(ReplyEntity.class);
        verify(replyRepository).save(captor.capture());
        assertNull(captor.getValue().email(), "Entity email passed to repository must also be null");
    }

    @Test
    @DisplayName("Create reply for non-existent parent post should throw PostNotFoundException")
    void testCreateReplyWhenParentPostNotFoundThrowsException() {
        CreateReplyRequest request = new CreateReplyRequest("Bob", null, "Message");
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        PostNotFoundException ex = assertThrows(
                PostNotFoundException.class,
                () -> postService.createReply(999L, request),
                "Should throw PostNotFoundException when post does not exist");

        assertEquals(999L, ex.getPostId());
        verify(replyRepository, never()).save(any());
    }

    @ParameterizedTest(name = "invalid reply arguments (blank/empty/null): name=''{0}'', message=''{1}''")
    @CsvSource(
            value = {
                "'   ', 'Valid Message'",
                "'', 'Valid Message'",
                "NIL, 'Valid Message'",
                "'Valid Name', '   '",
                "'Valid Name', ''",
                "'Valid Name', NIL"
            },
            nullValues = {"NIL"})
    @DisplayName(
            "Precondition violation: blank, empty, or null name or message in createReply must throw IllegalArgumentException")
    void testCreateReplyRejectsBlankFields(String name, String message) {
        CreateReplyRequest request = new CreateReplyRequest(name, null, message);

        assertThrows(
                IllegalArgumentException.class,
                () -> postService.createReply(10L, request),
                "Blank, empty, or null name or message must be rejected");
        verify(postRepository, never()).findById(any());
        verify(replyRepository, never()).save(any());
    }

    @ParameterizedTest(name = "invalid postId: {0}")
    @ValueSource(longs = {0L, -1L, -100L})
    @DisplayName("Precondition violation: non-positive postId in createReply must throw IllegalArgumentException")
    void testCreateReplyRejectsNonPositivePostId(long invalidPostId) {
        CreateReplyRequest request = new CreateReplyRequest("Bob", null, "Message");

        assertThrows(IllegalArgumentException.class, () -> postService.createReply(invalidPostId, request));
        verify(postRepository, never()).findById(any());
        verify(replyRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "Precondition violation: null command or null postId in createReply must throw IllegalArgumentException")
    void testCreateReplyRejectsNullCommandOrPostId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> postService.createReply(null, new CreateReplyRequest("Bob", null, "Msg")));
        assertThrows(IllegalArgumentException.class, () -> postService.createReply(1L, null));
        verify(postRepository, never()).findById(any());
        verify(replyRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "getPagedPosts must batch fetch replies in chronological order and guarantee empty list on zero replies")
    void testGetPagedPostsBatchFetchesReplies() {
        LocalDateTime now = LocalDateTime.now();
        PostEntity post1 = new PostEntity(1L, "User1", null, "Title1", "Message1", now);
        PostEntity post2 = new PostEntity(2L, "User2", null, "Title2", "Message2", now.minusMinutes(5));
        PostEntity post3 = new PostEntity(3L, "User3", null, "Title3", "Message3", now.minusMinutes(10));

        Page<PostEntity> mockPage = Page.of(List.of(post1, post2, post3), Pageable.from(0, 50), 3L);
        when(postRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        ReplyEntity reply1toPost1 =
                new ReplyEntity(101L, 1L, "Replier1", null, "Reply 1 to Post 1", now.plusSeconds(10));
        ReplyEntity reply2toPost1 =
                new ReplyEntity(102L, 1L, "Replier2", null, "Reply 2 to Post 1", now.plusSeconds(20));
        ReplyEntity reply1toPost2 =
                new ReplyEntity(103L, 2L, "Replier3", null, "Reply 1 to Post 2", now.minusMinutes(4));

        when(replyRepository.findByPostIdInOrderByCreatedAtAsc(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(reply1toPost2, reply1toPost1, reply2toPost1));

        PagedPostResponse response = postService.getPagedPosts(0, 50);

        assertNotNull(response);
        assertEquals(3, response.items().size());

        PostResponse post1Response = response.items().get(0);
        assertEquals(1L, post1Response.id());
        assertEquals(2, post1Response.replies().size(), "Post 1 must have 2 replies");
        assertEquals(101L, post1Response.replies().get(0).id());
        assertEquals(102L, post1Response.replies().get(1).id());

        PostResponse post2Response = response.items().get(1);
        assertEquals(2L, post2Response.id());
        assertEquals(1, post2Response.replies().size(), "Post 2 must have 1 reply");
        assertEquals(103L, post2Response.replies().get(0).id());

        PostResponse post3Response = response.items().get(2);
        assertEquals(3L, post3Response.id());
        assertNotNull(post3Response.replies(), "Post 3 replies must never be null");
        assertTrue(
                post3Response.replies().isEmpty(),
                "Post 3 replies must be guaranteed empty list [] when 0 replies exist");

        verify(replyRepository).findByPostIdInOrderByCreatedAtAsc(List.of(1L, 2L, 3L));
    }
}
