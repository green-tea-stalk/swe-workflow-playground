import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreatePostRequest, PagedPostResponse, PostResponse } from '../models/post.model';

/**
 * Service providing HTTP REST client communication with the bulletin board backend API.
 */
@Injectable({
  providedIn: 'root',
})
export class PostApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/posts';

  /**
   * Fetches a paginated page of bulletin board posts in reverse-chronological order.
   *
   * @param page zero-based page index (defaults to 0)
   * @param size page size limit (defaults to 50)
   * @returns an Observable emitting the {@link PagedPostResponse}
   */
  getPosts(page: number = 0, size: number = 50): Observable<PagedPostResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedPostResponse>(this.baseUrl, { params });
  }

  /**
   * Submits a new bulletin board post to the backend service.
   *
   * @param request the post creation payload
   * @returns an Observable emitting the created {@link PostResponse}
   */
  createPost(request: CreatePostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(this.baseUrl, request);
  }
}
