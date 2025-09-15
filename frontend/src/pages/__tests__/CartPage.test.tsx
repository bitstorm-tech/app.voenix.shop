import CartPage from '@/pages/Cart';
import type { Article } from '@/types/article';
import type { CartDto } from '@/types/cart';
import { render, screen, within } from '@testing-library/react';

jest.mock('@/hooks/queries/useAuth', () => ({
  useSession: jest.fn(),
}));

jest.mock('@/hooks/queries/useCart', () => ({
  useCart: jest.fn(),
  useUpdateCartItem: jest.fn(),
  useRemoveCartItem: jest.fn(),
}));

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => jest.fn(),
}));

jest.mock('@/lib/imagePreloader', () => ({
  imagePreloader: {
    preloadImages: jest.fn().mockResolvedValue(undefined),
  },
}));

const mockUseSession = jest.requireMock('@/hooks/queries/useAuth').useSession as jest.Mock;
const mockUseCartModule = jest.requireMock('@/hooks/queries/useCart');
const mockUseCart = mockUseCartModule.useCart as jest.Mock;
const mockUseUpdateItem = mockUseCartModule.useUpdateCartItem as jest.Mock;
const mockUseRemoveItem = mockUseCartModule.useRemoveCartItem as jest.Mock;

const baseArticle: Article = {
  id: 101,
  name: 'Snowy Mug',
  descriptionShort: 'Short description',
  descriptionLong: 'Long description',
  active: true,
  articleType: 'MUG',
  categoryId: 1,
  categoryName: 'Mugs',
  subcategoryId: undefined,
  subcategoryName: undefined,
  supplierId: undefined,
  supplierName: undefined,
  supplierArticleName: undefined,
  supplierArticleNumber: undefined,
  mugVariants: [],
  shirtVariants: [],
  mugDetails: undefined,
  shirtDetails: undefined,
  costCalculation: undefined,
  createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
  updatedAt: new Date('2025-01-02T00:00:00Z').toISOString(),
};

const createCartFixture = (): CartDto => {
  const now = new Date('2025-01-03T00:00:00Z').toISOString();
  return {
    id: 1,
    userId: 42,
    status: 'ACTIVE',
    version: 1,
    expiresAt: null,
    items: [
      {
        id: 11,
        article: baseArticle,
        variant: {
          id: 201,
          articleId: baseArticle.id,
          colorCode: '#ffffff',
          exampleImageUrl: null,
          supplierArticleNumber: null,
          isDefault: true,
          exampleImageFilename: null,
        },
        quantity: 2,
        priceAtTime: 1000,
        originalPrice: 1200,
        articlePriceAtTime: 1000,
        promptPriceAtTime: 250,
        articleOriginalPrice: 1200,
        promptOriginalPrice: 300,
        hasPriceChanged: true,
        hasPromptPriceChanged: true,
        totalPrice: (1000 + 250) * 2,
        customData: {},
        generatedImageId: undefined,
        generatedImageFilename: undefined,
        promptId: 501,
        promptTitle: 'Winter Prompt',
        position: 0,
        createdAt: now,
        updatedAt: now,
      },
    ],
    totalItemCount: 2,
    totalPrice: (1000 + 250) * 2,
    isEmpty: false,
    createdAt: now,
    updatedAt: now,
  };
};

describe('CartPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();

    mockUseSession.mockReturnValue({
      data: { authenticated: true },
      isLoading: false,
    });

    mockUseCart.mockReturnValue({
      data: createCartFixture(),
      isLoading: false,
      error: null,
    });

    mockUseUpdateItem.mockReturnValue({ mutateAsync: jest.fn() });
    mockUseRemoveItem.mockReturnValue({ mutateAsync: jest.fn() });
  });

  it('renders prompt pricing breakdown and accurate totals', () => {
    render(<CartPage />);

    expect(screen.getByText('Winter Prompt price:')).toBeInTheDocument();
    expect(screen.getByText('$2.50')).toBeInTheDocument();
    expect(screen.getByText('(was $3.00)')).toBeInTheDocument();
    expect(screen.getByText('(was $12.00)')).toBeInTheDocument();
    expect(screen.getByText('Subtotal: $25.00')).toBeInTheDocument();

    const summary = screen.getByText('Order Summary').closest('div');
    expect(summary).not.toBeNull();
    const summaryScope = within(summary as HTMLElement);
    expect(summaryScope.getByText('Subtotal (2 items)')).toBeInTheDocument();
    expect(summaryScope.getAllByText('$25.00').length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText('Checkout • $25.00')).toBeInTheDocument();
  });
});
