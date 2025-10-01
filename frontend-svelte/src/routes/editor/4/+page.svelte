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
		metaTitle: 'Editor Step 4 – Personal Information | Voenix Shop'
	};

	const currentStepId = 'user-data';
	const currentIndex = steps.findIndex((step) => step.id === currentStepId);
</script>

<StepShell {steps} {currentIndex} {detail}>
	<p class="text-sm text-gray-500 italic">
		TODO: Implement personal information form fields and validation.
	</p>
</StepShell>
