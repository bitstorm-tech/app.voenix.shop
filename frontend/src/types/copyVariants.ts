export interface MugWithVariantsSummary {
  id: number;
  name: string;
  supplierArticleName?: string;
  variants: MugVariantSummary[];
}

export interface MugVariantSummary {
  id: number;
  name: string;
  insideColorCode: string;
  outsideColorCode: string;
  articleVariantNumber?: string;
  exampleImageUrl?: string;
  active: boolean;
}

export interface CopyVariantsRequest {
  variantIds: number[];
}
