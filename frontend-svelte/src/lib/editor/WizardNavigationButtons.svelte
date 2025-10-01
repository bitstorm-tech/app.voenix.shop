<script lang="ts">
	import { Button } from '$lib/components/ui/button';

	type Props = {
		canGoPrevious?: boolean;
		canGoNext?: boolean;
		backLabel?: string;
		nextLabel?: string;
		previousHref?: string;
		nextHref?: string;
	};

	let {
		canGoPrevious = false,
		canGoNext = true,
		backLabel = 'Back',
		nextLabel = 'Next',
		previousHref,
		nextHref
	}: Props = $props();

	const effectivePreviousHref = canGoPrevious && previousHref ? previousHref : undefined;
	const effectiveNextHref = canGoNext && nextHref ? nextHref : undefined;
	const previousAttrs = effectivePreviousHref
		? ({ href: effectivePreviousHref } as const)
		: { type: 'button' as const, disabled: true };
	const nextAttrs = effectiveNextHref
		? ({ href: effectiveNextHref } as const)
		: { type: 'button' as const, disabled: true };
</script>

<div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
	<Button variant="outline" size="default" {...previousAttrs} aria-label={backLabel}>
		<svg
			aria-hidden="true"
			class="h-5 w-5"
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			stroke-width="2"
		>
			<path stroke-linecap="round" stroke-linejoin="round" d="M15 5l-7 7 7 7" />
		</svg>
		<span class="sr-only">{backLabel}</span>
		<span class="hidden sm:inline">{backLabel}</span>
	</Button>
	<Button size="default" {...nextAttrs} aria-label={nextLabel}>
		<span class="sr-only">{nextLabel}</span>
		<span class="hidden sm:inline">{nextLabel}</span>
		<svg
			aria-hidden="true"
			class="h-5 w-5"
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			stroke-width="2"
		>
			<path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
		</svg>
	</Button>
</div>
