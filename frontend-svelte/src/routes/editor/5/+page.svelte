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
		metaTitle: 'Editor Step 5 – Generate Magic | Voenix Shop'
	};

	const currentStepId = 'image-generation';
	const currentIndex = steps.findIndex((step) => step.id === currentStepId);
</script>

<StepShell {steps} {currentIndex} {detail}>
	<p class="text-sm text-gray-500 italic">TODO: Render generation status and AI image gallery.</p>
</StepShell>
