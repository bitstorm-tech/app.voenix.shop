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
		metaTitle: 'Editor Step 1 – Upload & Crop | Voenix Shop'
	};

	const currentStepId = 'image-upload';
	const currentIndex = steps.findIndex((step) => step.id === currentStepId);
</script>

<StepShell {steps} {currentIndex} {detail}>
	<p class="text-sm text-gray-500 italic">
		TODO: Implement the upload & crop experience in Svelte.
	</p>
</StepShell>
