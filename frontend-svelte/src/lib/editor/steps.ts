export type StepDefinition = {
	id: string;
	label: string;
	headline: string;
	description: string;
	highlights: { title: string; description: string }[];
	placeholder: string;
	helper: string;
	path: string;
	metaTitle: string;
};

export const steps: StepDefinition[] = [
	{
		id: 'image-upload',
		label: 'Upload & Crop Image',
		headline: 'Upload & crop your favorite photo',
		description:
			'Drag & drop a file or browse from your library to kick off the customization journey.',
		highlights: [
			{
				title: 'Flexible file support',
				description: 'PNG, JPG, GIF, or WEBP up to 4MB keeps the workflow lightweight and fast.'
			},
			{
				title: 'Smart crop guidance',
				description:
					'Frame the printable area of the mug with a guided crop overlay before moving on.'
			}
		],
		placeholder: 'Workspace for the upload & crop interface lives here in the Svelte version.',
		helper: 'When the image feels ready, continue to explore style prompts in the next step.',
		path: '/editor/1',
		metaTitle: 'Editor Step 1 – Upload & Crop | Voenix Shop'
	},
	{
		id: 'prompt-selection',
		label: 'Select Style',
		headline: 'Choose a style prompt to inspire the AI',
		description: 'Browse curated looks that transform your uploaded photo into something magical.',
		highlights: [
			{
				title: 'Curated inspiration',
				description:
					'Preview thumbnails help you understand how each prompt feels before making a pick.'
			},
			{
				title: 'Quick filtering',
				description:
					'Slice the catalog by mood or theme to surface the most relevant styles instantly.'
			}
		],
		placeholder: 'Prompt selection cards and filters render in this area.',
		helper: 'After locking in a prompt, you will choose the physical mug that matches the design.',
		path: '/editor/2',
		metaTitle: 'Editor Step 2 – Select Style | Voenix Shop'
	},
	{
		id: 'mug-selection',
		label: 'Choose Mug',
		headline: 'Select the mug that brings the design to life',
		description: 'Compare sizes, finishes, and pricing to find the perfect canvas for the artwork.',
		highlights: [
			{
				title: 'Side-by-side variants',
				description: 'Visualize color options, capacities, and finishes to make a confident choice.'
			},
			{
				title: 'Merch-ready details',
				description:
					'Callouts like dishwasher-safe or limited edition badges are easy to surface here.'
			}
		],
		placeholder: 'Mug comparison tiles and feature callouts appear in this section.',
		helper: 'Next up, collect customer details to keep the checkout experience smooth.',
		path: '/editor/3',
		metaTitle: 'Editor Step 3 – Choose Mug | Voenix Shop'
	},
	{
		id: 'user-data',
		label: 'Personal Information',
		headline: 'Confirm the customer details for fulfillment',
		description: 'Keep the form lightweight while explaining why each field matters.',
		highlights: [
			{
				title: 'Streamlined entry',
				description:
					'Prefill account data when available but still allow guests to proceed without friction.'
			},
			{
				title: 'Trust-first messaging',
				description:
					'Small helper text reassures customers that their information is handled securely.'
			}
		],
		placeholder: 'Form inputs, validation states, and helper copy are rendered within this block.',
		helper: 'With details captured, the generator can focus on delivering great preview images.',
		path: '/editor/4',
		metaTitle: 'Editor Step 4 – Personal Information | Voenix Shop'
	},
	{
		id: 'image-generation',
		label: 'Generate Magic',
		headline: 'Preview AI-generated variations',
		description:
			'Surface loading feedback while the AI produces design alternatives to choose from.',
		highlights: [
			{
				title: 'Optimistic loading states',
				description: 'Friendly copy keeps customers engaged while waiting for fresh designs.'
			},
			{
				title: 'Selectable gallery',
				description:
					'Use a card grid or carousel so shoppers can instantly pick their favorite result.'
			}
		],
		placeholder: 'Generated image cards or a carousel would live here with selection controls.',
		helper: 'Once a favorite design is selected, move forward to place it on the chosen mug.',
		path: '/editor/5',
		metaTitle: 'Editor Step 5 – Generate Magic | Voenix Shop'
	},
	{
		id: 'preview',
		label: 'Preview Product',
		headline: 'Fine-tune placement and review the order summary',
		description: 'Provide a canvas for positioning artwork alongside a live product preview.',
		highlights: [
			{
				title: 'Interactive canvas',
				description: 'Drag, zoom, and align overlays to perfect the final print area.'
			},
			{
				title: 'At-a-glance recap',
				description: 'Summaries reinforce the customer’s selections and pricing before checkout.'
			}
		],
		placeholder: 'The final preview stage fills this area with mug mockups and order details.',
		helper: 'When everything looks right, the customer is ready to add the design to their cart.',
		path: '/editor/6',
		metaTitle: 'Editor Step 6 – Preview Product | Voenix Shop'
	}
];
