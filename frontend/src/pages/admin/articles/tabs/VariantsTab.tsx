import type {
  ArticleMugVariant,
  ArticleShirtVariant,
  ArticleType,
  CreateArticleMugVariantRequest,
  CreateArticleShirtVariantRequest,
} from '@/types/article';
import MugVariantsTab from './MugVariantsTab';
import ShirtVariantsTab from './ShirtVariantsTab';

interface VariantsTabProps {
  articleId?: number;
  articleType: ArticleType;
  mugVariants?: ArticleMugVariant[];
  shirtVariants?: ArticleShirtVariant[];
  temporaryMugVariants?: CreateArticleMugVariantRequest[];
  temporaryShirtVariants?: CreateArticleShirtVariantRequest[];
  onAddTemporaryMugVariant?: (variant: CreateArticleMugVariantRequest) => void;
  onAddTemporaryShirtVariant?: (variant: CreateArticleShirtVariantRequest) => void;
  onDeleteTemporaryMugVariant?: (index: number) => void;
  onDeleteTemporaryShirtVariant?: (index: number) => void;
  onUpdateTemporaryMugVariant?: (index: number, variant: CreateArticleMugVariantRequest) => void;
  onUpdateTemporaryShirtVariant?: (index: number, variant: CreateArticleShirtVariantRequest) => void;
}

export default function VariantsTab({
  articleId,
  articleType,
  mugVariants = [],
  shirtVariants = [],
  temporaryMugVariants = [],
  temporaryShirtVariants = [],
  onAddTemporaryMugVariant,
  onAddTemporaryShirtVariant,
  onDeleteTemporaryMugVariant,
  onDeleteTemporaryShirtVariant,
  onUpdateTemporaryMugVariant,
  onUpdateTemporaryShirtVariant,
}: VariantsTabProps) {
  switch (articleType) {
    case 'MUG':
      return (
        <MugVariantsTab
          articleId={articleId}
          variants={mugVariants}
          temporaryVariants={temporaryMugVariants}
          onAddTemporaryVariant={onAddTemporaryMugVariant}
          onDeleteTemporaryVariant={onDeleteTemporaryMugVariant}
          onUpdateTemporaryVariant={onUpdateTemporaryMugVariant}
        />
      );
    case 'SHIRT':
      return (
        <ShirtVariantsTab
          articleId={articleId}
          variants={shirtVariants}
          temporaryVariants={temporaryShirtVariants}
          onAddTemporaryVariant={onAddTemporaryShirtVariant}
          onDeleteTemporaryVariant={onDeleteTemporaryShirtVariant}
          onUpdateTemporaryVariant={onUpdateTemporaryShirtVariant}
        />
      );
    default:
      return null;
  }
}
