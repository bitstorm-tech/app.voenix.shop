export interface ValueAddedTax {
  id: number;
  name: string;
  percent: number;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateValueAddedTaxRequest {
  name: string;
  percent: number;
  description?: string | null;
}

export interface UpdateValueAddedTaxRequest {
  name?: string;
  percent?: number;
  description?: string | null;
}
