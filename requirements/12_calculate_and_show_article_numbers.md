# Calculate and show article numbers

## Description
Article numbers are a combination/concatenation of different table IDs. These article numbers must be shown in some places in the frontend. Article numbers exist for articles and prompts.

Here are the formating of them:
- Article: `A-{article_category_id}-{article_subcategory_id|1000}-{article_id}-{variant_id}`
- Prompt: `P-{prompt_category_id}-{prompt_subcategory_id|1000}-{prompt_id}`

## Requirements Frontend

#### Requirement F1: Show the article number in the NewOrEditArticle page
- Add a new field (not editable) for the article number inside the `Basic Information` Card to the `General` tab.
- When editing an existing article, show the article number (see the format in the description) in the field.
- When creating a new article, indicate that the article number is visible after saving the article.

#### Requirement F2: Show the article number in the NewOrEditPrompt page
- Add a new field (not editable) for the article number to the NewOrEditPrompt page.
- When editing an existing article, show the article number (see the format in the description) in the field.
- When creating a new article, indicate that the article number is visible after saving the article.
