<script lang="ts">
	import type { Snippet } from 'svelte';
	import WizardNavigationButtons from './WizardNavigationButtons.svelte';
	import WizardStepIndicator, { type WizardStepMeta } from './WizardStepIndicator.svelte';

	export type StepSummary = {
		id: string;
		label: string;
		path: string;
	};

	export type Highlight = {
		title: string;
		description: string;
	};

	export type StepDetail = {
		headline: string;
		description: string;
		highlights?: Highlight[];
		placeholder: string;
		helper: string;
		metaTitle: string;
	};

	let {
		steps,
		currentIndex,
		detail,
		children
	}: {
		steps: StepSummary[];
		currentIndex: number;
		detail: StepDetail;
		children?: Snippet;
	} = $props();

	const safeIndex = (() => {
		if (!Number.isFinite(currentIndex)) return 0;
		const clamped = Math.min(Math.max(Math.trunc(currentIndex), 0), Math.max(steps.length - 1, 0));
		return clamped;
	})();

	const indicatorSteps: WizardStepMeta[] = steps.map(({ id, label }) => ({ id, label }));
	const previousHref = safeIndex > 0 ? steps[safeIndex - 1]?.path : undefined;
	const nextHref = safeIndex < steps.length - 1 ? steps[safeIndex + 1]?.path : undefined;
	const canGoPrevious = Boolean(previousHref);
	const canGoNext = Boolean(nextHref);
	const nextLabel = safeIndex === steps.length - 1 ? 'Finish' : 'Next Step';
	const highlights = detail.highlights ?? [];
	const hasHighlights = highlights.length > 0;
</script>

<svelte:head>
	<title>{detail.metaTitle}</title>
</svelte:head>

<div class="min-h-screen bg-gray-50 pb-32">
	<div class="mx-auto max-w-5xl px-4 pt-8 lg:px-6">
		<section class="mb-8 text-center">
			<h1 class="text-3xl font-bold text-gray-900 sm:text-4xl">Create Your Custom Mug</h1>
			<p class="mt-2 text-sm text-gray-600 sm:text-base">
				Follow the steps below to design your personalized mug.
			</p>
		</section>

		<section class="mb-8">
			<WizardStepIndicator steps={indicatorSteps} currentIndex={safeIndex} />
		</section>

		<article class="mb-12 min-h-[400px] rounded-lg bg-white p-6 shadow-sm ring-1 ring-gray-100">
			<header class="mb-6 space-y-2 text-center sm:text-left">
				<p class="text-xs font-semibold tracking-wide text-blue-600 uppercase">
					Step {safeIndex + 1} of {steps.length}
				</p>
				<h2 class="text-2xl font-semibold text-gray-900 sm:text-3xl">{detail.headline}</h2>
				<p class="text-sm text-gray-600 sm:text-base">{detail.description}</p>
			</header>

			<div class="space-y-6">
				{#if hasHighlights}
					<div class="grid gap-4 sm:grid-cols-2">
						{#each highlights as highlight}
							<div class="rounded-lg border border-gray-200 bg-gray-50 p-4 text-left">
								<h3 class="text-sm font-semibold text-gray-900">{highlight.title}</h3>
								<p class="mt-1 text-xs text-gray-600 sm:text-sm">{highlight.description}</p>
							</div>
						{/each}
					</div>
				{/if}

				<div
					class="space-y-4 rounded-lg border border-dashed border-gray-300 bg-slate-50 p-6 text-sm text-gray-600"
				>
					<p>{detail.placeholder}</p>
					{@render children?.()}
				</div>

				<p class="text-xs text-gray-500 sm:text-sm">{detail.helper}</p>
			</div>
		</article>
	</div>

	<div class="fixed inset-x-0 bottom-0 bg-gradient-to-t from-white/95 to-white/40 py-4">
		<div class="mx-auto max-w-5xl px-4 lg:px-6">
			<div
				class="rounded-lg border border-gray-200 bg-white/90 p-4 shadow-lg backdrop-blur supports-[backdrop-filter]:bg-white/70"
			>
				<WizardNavigationButtons
					{canGoPrevious}
					{canGoNext}
					backLabel="Back"
					{nextLabel}
					{previousHref}
					{nextHref}
				/>
			</div>
		</div>
	</div>
</div>
