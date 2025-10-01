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
		metaTitle: 'Editor Step 6 – Preview Product | Voenix Shop'
	};

	const currentStepId = 'preview';
	const currentIndex = steps.findIndex((step) => step.id === currentStepId);
</script>

<StepShell {steps} {currentIndex} {detail}>
	<p class="text-sm text-gray-500 italic">TODO: Show final mockup, summary, and checkout cta.</p>
</StepShell>
