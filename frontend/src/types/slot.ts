export interface SlotType {
  id: number;
  name: string;
  position: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Slot {
  id: number;
  slotTypeId: number;
  slotType?: SlotType;
  name: string;
  prompt: string;
  description?: string;
  exampleImageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}
