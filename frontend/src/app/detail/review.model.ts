export interface Review {
    id?: number;
    userId: number;
    bookId: number;
    rating: number;
    content: string;
}