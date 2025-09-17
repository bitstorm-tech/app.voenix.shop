import type { Article } from '@/types/article';
import { useTranslation } from 'react-i18next';
import { MugVariantList } from './MugVariantList';
import { ShirtVariantList } from './ShirtVariantList';

interface ArticleVariantsProps {
  article: Article;
}

export function ArticleVariants({ article }: ArticleVariantsProps) {
  const { t } = useTranslation('adminArticles');

  if (article.articleType === 'MUG' && article.mugVariants) {
    return <MugVariantList variants={article.mugVariants} article={article} />;
  }

  if (article.articleType === 'SHIRT' && article.shirtVariants) {
    return <ShirtVariantList variants={article.shirtVariants} />;
  }

  return <div className="text-muted-foreground py-4 text-center text-sm">{t('variants.none')}</div>;
}
