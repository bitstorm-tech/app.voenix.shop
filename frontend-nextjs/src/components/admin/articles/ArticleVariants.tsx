import type { Article } from '@/types/article';
import { MugVariantList } from './MugVariantList';
import { ShirtVariantList } from './ShirtVariantList';

interface ArticleVariantsProps {
  article: Article;
}

export function ArticleVariants({ article }: ArticleVariantsProps) {
  if (article.articleType === 'MUG' && article.mugVariants) {
    return <MugVariantList variants={article.mugVariants} article={article} />;
  }

  if (article.articleType === 'SHIRT' && article.shirtVariants) {
    return <ShirtVariantList variants={article.shirtVariants} article={article} />;
  }

  return <div className="text-muted-foreground py-4 text-center text-sm">No variants available</div>;
}