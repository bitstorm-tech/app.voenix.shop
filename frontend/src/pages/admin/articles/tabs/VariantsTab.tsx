import type {
  ArticleMugVariant,
  ArticlePillowVariant,
  ArticleShirtVariant,
  ArticleType,
  CreateArticleMugVariantRequest,
  CreateArticlePillowVariantRequest,
  CreateArticleShirtVariantRequest,
} from '@/types/article';
import MugVariantsTab from './MugVariantsTab';
import PillowVariantsTab from './PillowVariantsTab';
import ShirtVariantsTab from './ShirtVariantsTab';

interface VariantsTabProps {
  articleId?: number;
  articleType: ArticleType;
  mugVariants?: ArticleMugVariant[];
  shirtVariants?: ArticleShirtVariant[];
  pillowVariants?: ArticlePillowVariant[];
  temporaryMugVariants?: CreateArticleMugVariantRequest[];
  temporaryShirtVariants?: CreateArticleShirtVariantRequest[];
  temporaryPillowVariants?: CreateArticlePillowVariantRequest[];
  onAddTemporaryMugVariant?: (variant: CreateArticleMugVariantRequest) => void;
  onAddTemporaryShirtVariant?: (variant: CreateArticleShirtVariantRequest) => void;
  onAddTemporaryPillowVariant?: (variant: CreateArticlePillowVariantRequest) => void;
  onDeleteTemporaryMugVariant?: (index: number) => void;
  onDeleteTemporaryShirtVariant?: (index: number) => void;
  onDeleteTemporaryPillowVariant?: (index: number) => void;
  onUpdateTemporaryMugVariant?: (index: number, variant: CreateArticleMugVariantRequest) => void;
  onUpdateTemporaryShirtVariant?: (index: number, variant: CreateArticleShirtVariantRequest) => void;
  onUpdateTemporaryPillowVariant?: (index: number, variant: CreateArticlePillowVariantRequest) => void;
}

export default function VariantsTab({
  articleId,
  articleType,
  mugVariants = [],
  shirtVariants = [],
  pillowVariants = [],
  temporaryMugVariants = [],
  temporaryShirtVariants = [],
  temporaryPillowVariants = [],
  onAddTemporaryMugVariant,
  onAddTemporaryShirtVariant,
  onAddTemporaryPillowVariant,
  onDeleteTemporaryMugVariant,
  onDeleteTemporaryShirtVariant,
  onDeleteTemporaryPillowVariant,
  onUpdateTemporaryMugVariant,
  onUpdateTemporaryShirtVariant,
  onUpdateTemporaryPillowVariant,
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
    case 'PILLOW':
      return (
        <PillowVariantsTab
          articleId={articleId}
          variants={pillowVariants}
          temporaryVariants={temporaryPillowVariants}
          onAddTemporaryVariant={onAddTemporaryPillowVariant}
          onDeleteTemporaryVariant={onDeleteTemporaryPillowVariant}
          onUpdateTemporaryVariant={onUpdateTemporaryPillowVariant}
        />
      );
    default:
      return null;
  }
}
