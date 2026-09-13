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
@DisplayName("Unit test suite for PostService")
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
    @DisplayName("Valid post request: should trim strings, set UTC timestamp, persist entity, and return PostResponse")
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

    @ParameterizedTest(name = "blank email: ''{0}'' should normalize to null")
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName("Blank or whitespace-only email should normalize to null before persistence")
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

        assertNull(response.email(), "Blank email must be normalized to null");

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        assertNull(captor.getValue().email());
    }

    @Test
    @DisplayName("Null email: should safely persist null email")
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
        assertNull(captor.getValue().email(), "Entity email passed to repository must also be null");
    }

    @Test
    @DisplayName("Precondition violation: null request object must throw IllegalArgumentException")
    void testCreatePostRejectsNullCommand() {
        assertThrows(IllegalArgumentException.class, () -> postService.createPost(null),
                "Null request must be rejected");
        verify(postRepository, never()).save(any());
    }

    @ParameterizedTest(name = "invalid required field (blank/empty/null): name=''{0}'', title=''{1}'', message=''{2}''")
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
    @DisplayName("Precondition violation: blank, empty, or null required fields must throw IllegalArgumentException")
    void testCreatePostRejectsBlankFields(String name, String title, String message) {
        CreatePostRequest request = new CreatePostRequest(name, null, title, message);

        assertThrows(IllegalArgumentException.class, () -> postService.createPost(request),
                "Blank, empty, or null required fields must be rejected as precondition violation");
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Valid pagination request: should pass Pageable sorted descending and return PagedPostResponse")
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
    @DisplayName("Defensive contract: should guarantee empty list [] instead of null in PagedPostResponse when zero records exist")
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
    @CsvSource({
            "-1, 50",
            "0, 0",
            "0, -5",
            "0, 51",
            "0, 100"
    })
    @DisplayName("Precondition violation: negative page or out-of-range size (not 1-50) must throw IllegalArgumentException")
    void testGetPagedPostsRejectsInvalidPagination(int page, int size) {
        assertThrows(IllegalArgumentException.class, () -> postService.getPagedPosts(page, size),
                "Out-of-range pagination arguments must be rejected");
        verify(postRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Precondition violation: passing null dependencies to constructor must throw NullPointerException")
    void testConstructorRejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> new PostService(null));
        assertThrows(NullPointerException.class, () -> new PostService(postRepository, null));
    }

    @Test
    @DisplayName("Default constructor: should instantiate successfully with system UTC clock")
    void testDefaultConstructorInstantiates() {
        PostService defaultService = new PostService(postRepository);
        assertNotNull(defaultService);
    }
}
