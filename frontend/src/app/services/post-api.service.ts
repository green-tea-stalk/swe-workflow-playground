import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreatePostRequest, PagedPostResponse, PostResponse } from '../models/post.model';
import { LocaleService } from './locale.service';

/**
 * Service providing HTTP REST client communication with the bulletin board backend API.
 * Attaches the active application locale in the Accept-Language HTTP header.
 */
@Injectable({
  providedIn: 'root',
})
export class PostApiService {
  private readonly http = inject(HttpClient);
  private readonly localeService = inject(LocaleService);
  private readonly baseUrl = '/api/posts';

  /**
   * Constructs HTTP request headers containing the active application locale.
   *
   * @returns HttpHeaders configured with Accept-Language
   */
  private createHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Accept-Language': this.localeService.getActiveLocale(),
    });
  }

  /**
   * Fetches a paginated page of bulletin board posts in reverse-chronological order.
   *
   * @param page zero-based page index (defaults to 0)
   * @param size page size limit (defaults to 50)
   * @returns an Observable emitting the {@link PagedPostResponse}, or emitting {@link HttpErrorResponse} on failure
   */
  getPosts(page: number = 0, size: number = 50): Observable<PagedPostResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedPostResponse>(this.baseUrl, {
      params,
      headers: this.createHeaders(),
    });
  }

  /**
   * Submits a new bulletin board post to the backend service.
   *
   * @param request the post creation payload
   * @returns an Observable emitting the created {@link PostResponse}, or emitting {@link HttpErrorResponse} on failure
   */
  createPost(request: CreatePostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(this.baseUrl, request, {
      headers: this.createHeaders(),
    });
  }
}

