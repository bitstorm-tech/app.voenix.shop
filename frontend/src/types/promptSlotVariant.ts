export interface PromptSlotType {
  id: number;
  name: string;
  position: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PromptSlotVariant {
  id: number;
  promptSlotTypeId: number;
  promptSlotType?: PromptSlotType;
  name: string;
  prompt: string;
  description?: string;
  exampleImageUrl?: string;
  llm: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProviderLLM {
  provider: string;
  llm: string;
  friendlyName: string;
}
