import type { Country } from './country';

export interface Supplier {
  id: number;
  name: string | null;
  title: string | null;
  firstName: string | null;
  lastName: string | null;
  street: string | null;
  houseNumber: string | null;
  city: string | null;
  postalCode: number | null;
  country: Country | null;
  phoneNumber1: string | null;
  phoneNumber2: string | null;
  phoneNumber3: string | null;
  email: string | null;
  website: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSupplierRequest {
  name: string | null;
  title: string | null;
  firstName: string | null;
  lastName: string | null;
  street: string | null;
  houseNumber: string | null;
  city: string | null;
  postalCode: number | null;
  countryId: number | null;
  phoneNumber1: string | null;
  phoneNumber2: string | null;
  phoneNumber3: string | null;
  email: string | null;
  website: string | null;
}

export interface UpdateSupplierRequest {
  name: string | null;
  title: string | null;
  firstName: string | null;
  lastName: string | null;
  street: string | null;
  houseNumber: string | null;
  city: string | null;
  postalCode: number | null;
  countryId: number | null;
  phoneNumber1: string | null;
  phoneNumber2: string | null;
  phoneNumber3: string | null;
  email: string | null;
  website: string | null;
}
