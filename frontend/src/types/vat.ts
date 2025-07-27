export interface ValueAddedTax {
  id: number;
  name: string;
  percent: number;
  description?: string | null;
  isDefault: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateValueAddedTaxRequest {
  name: string;
  percent: number;
  description?: string | null;
  isDefault?: boolean;
}

export interface UpdateValueAddedTaxRequest {
  name?: string;
  percent?: number;
  description?: string | null;
  isDefault?: boolean;
}
