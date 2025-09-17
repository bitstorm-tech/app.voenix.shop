export const WIZARD_STEPS = ['image-upload', 'prompt-selection', 'mug-selection', 'user-data', 'image-generation', 'preview'] as const;

export type WizardStep = (typeof WIZARD_STEPS)[number];

export const STEP_LABEL_KEYS: Record<WizardStep, string> = {
  'image-upload': 'steps.labels.imageUpload',
  'prompt-selection': 'steps.labels.promptSelection',
  'mug-selection': 'steps.labels.mugSelection',
  'user-data': 'steps.labels.userData',
  'image-generation': 'steps.labels.imageGeneration',
  preview: 'steps.labels.preview',
} as const;

export const STEP_INDEX: Record<WizardStep, number> = {
  'image-upload': 0,
  'prompt-selection': 1,
  'mug-selection': 2,
  'user-data': 3,
  'image-generation': 4,
  preview: 5,
} as const;

import { MugOption } from './types';

export const MUG_OPTIONS: MugOption[] = [
  {
    id: 3,
    name: 'Classic White Mug',
    price: 12.99,
    image: '/images/mugs/classic-white.jpg',
    capacity: '11oz',
    special: undefined,
    dishwasher_safe: true,
    height_mm: 95,
    diameter_mm: 82,
    filling_quantity: '325ml',
    description_short: undefined,
  },
  {
    id: 4,
    name: 'Black Matte Mug',
    price: 14.99,
    image: '/images/mugs/black-matte.jpg',
    capacity: '11oz',
    special: undefined,
    dishwasher_safe: true,
    height_mm: 95,
    diameter_mm: 82,
    filling_quantity: '325ml',
    description_short: undefined,
  },
  {
    id: 5,
    name: 'Color Changing Mug',
    price: 19.99,
    image: '/images/mugs/color-changing.jpg',
    capacity: '11oz',
    special: 'Heat Reactive',
    dishwasher_safe: false,
    height_mm: 95,
    diameter_mm: 82,
    filling_quantity: '325ml',
    description_short: undefined,
  },
  {
    id: 6,
    name: 'Travel Mug',
    price: 24.99,
    image: '/images/mugs/travel-mug.jpg',
    capacity: '16oz',
    special: 'Insulated',
    dishwasher_safe: true,
    height_mm: 150,
    diameter_mm: 75,
    filling_quantity: '470ml',
    description_short: undefined,
  },
];

export const CROP_ASPECT_RATIOS = [
  { label: 'Square (1:1)', value: 1 },
  { label: 'Portrait (3:4)', value: 3 / 4 },
  { label: 'Landscape (4:3)', value: 4 / 3 },
  { label: 'Free', value: undefined },
] as const;
