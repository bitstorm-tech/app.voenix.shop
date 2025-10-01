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
		metaTitle: 'Editor Step 3 – Choose Mug | Voenix Shop'
	};

	const currentStepId = 'mug-selection';
	const currentIndex = steps.findIndex((step) => step.id === currentStepId);
</script>

<StepShell {steps} {currentIndex} {detail}>
	<p class="text-sm text-gray-500 italic">
		TODO: Build mug comparison cards and selection actions.
	</p>
</StepShell>
