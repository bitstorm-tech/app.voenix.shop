export interface SlotType {
  id: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Slot {
  id: number;
  slotTypeId: number;
  slotType?: SlotType;
  name: string;
  prompt: string;
  createdAt?: string;
  updatedAt?: string;
}
