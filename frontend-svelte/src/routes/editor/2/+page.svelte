<script lang="ts">
	import StepShell, { type StepDetail, type StepSummary } from '$lib/editor/StepShell.svelte';

	const steps: StepSummary[] = [
		{ id: 'image-upload', label: 'Upload & Crop Image', path: '/editor/1' },
		{ id: 'prompt-selection', label: 'Select Style', path: '/editor/2' },
		{ id: 'mug-selection', label: 'Choose Mug', path: '/editor/3' },
		{ id: 'user-data', label: 'Personal Information', path: '/editor/4' },
		{ id: 'image-generation', label: 'Generate Magic', path: '/editor/5' },
		{ id: 'preview', label: 'Preview Product', path: '/editor/6' }
	];

	const detail: StepDetail = {
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
		metaTitle: 'Editor Step 2 – Select Style | Voenix Shop'
	};

	const currentStepId = 'prompt-selection';
	const currentIndex = steps.findIndex((step) => step.id === currentStepId);
</script>

<StepShell {steps} {currentIndex} {detail}>
	<p class="text-sm text-gray-500 italic">
		TODO: Surface prompt cards and filters for style selection.
	</p>
</StepShell>
