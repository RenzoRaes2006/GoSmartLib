import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Review } from './review.model';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private apiUrl = 'http://frontend.localhost/api';

  constructor(private http: HttpClient) { }

  // ik moet de pathen nog checken qua restful api
  getReviewsByBookId(bookId: number) {
    return this.http.get(`${this.apiUrl}/books/${bookId}/reviews`);
  }

  addReview(review: Review) {
    return this.http.post(`${this.apiUrl}/reviews`, review);
  }
}
