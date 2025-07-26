import { useArticleFormStore } from '@/stores/admin/articles/useArticleFormStore';
import { useVariantStore } from '@/stores/admin/articles/useVariantStore';
import MugVariantsTab from '../tabs/MugVariantsTab';
import ShirtVariantsTab from '../tabs/ShirtVariantsTab';

export default function VariantsTab() {
  const { article } = useArticleFormStore();
  const {
    mugVariants,
    shirtVariants,
    temporaryMugVariants,
    temporaryShirtVariants,
    addTemporaryMugVariant,
    addTemporaryShirtVariant,
    deleteTemporaryMugVariant,
    deleteTemporaryShirtVariant,
    updateTemporaryMugVariant,
    updateTemporaryShirtVariant,
  } = useVariantStore();

  if (!article.articleType) {
    return null;
  }

  switch (article.articleType) {
    case 'MUG':
      return (
        <MugVariantsTab
          articleId={article.id}
          variants={mugVariants}
          temporaryVariants={temporaryMugVariants}
          onAddTemporaryVariant={addTemporaryMugVariant}
          onDeleteTemporaryVariant={deleteTemporaryMugVariant}
          onUpdateTemporaryVariant={updateTemporaryMugVariant}
        />
      );
    case 'SHIRT':
      return (
        <ShirtVariantsTab
          articleId={article.id}
          variants={shirtVariants}
          temporaryVariants={temporaryShirtVariants}
          onAddTemporaryVariant={addTemporaryShirtVariant}
          onDeleteTemporaryVariant={deleteTemporaryShirtVariant}
          onUpdateTemporaryVariant={updateTemporaryShirtVariant}
        />
      );
    default:
      return null;
  }
}
