<script lang="ts">
	import { cn } from '$lib/utils/cn';

	export type WizardStepMeta = {
		id: string;
		label: string;
	};

	let { steps, currentIndex = 0 }: { steps: WizardStepMeta[]; currentIndex?: number } = $props();

	const totalSteps = steps.length;
	const clampedIndex = totalSteps > 0 ? Math.min(Math.max(currentIndex, 0), totalSteps - 1) : 0;
	const progress =
		totalSteps > 1 ? (clampedIndex / (totalSteps - 1)) * 100 : clampedIndex > 0 ? 100 : 0;
</script>

<div class="relative" role="list" aria-label="Editor progress">
	<div class="absolute top-5 left-0 h-0.5 w-full rounded-full bg-gray-200">
		<div
			class="h-full rounded-full bg-blue-600 transition-all duration-300"
			style:width={`${progress}%`}
		></div>
	</div>

	<div class="relative flex justify-between gap-4">
		{#each steps as step, index (step.id)}
			{@const isActive = index === clampedIndex}
			{@const isCompleted = index < clampedIndex}
			<div class="flex flex-1 flex-col items-center">
				<div
					class={cn(
						'relative z-10 flex h-10 w-10 items-center justify-center rounded-full border-2 bg-white text-sm font-semibold transition-colors',
						(isActive || isCompleted) && 'border-blue-600 bg-blue-600 text-white shadow-sm',
						!isActive && !isCompleted && 'border-gray-300 text-gray-500'
					)}
					aria-current={isActive ? 'step' : undefined}
				>
					{#if isCompleted}
						<svg
							aria-hidden="true"
							class="h-5 w-5"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2"
						>
							<path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
						</svg>
					{:else}
						<span>{index + 1}</span>
					{/if}
				</div>
				<span
					class={cn(
						'mt-2 max-w-[8rem] text-center text-xs leading-tight font-medium',
						isActive && 'text-blue-600',
						!isActive && isCompleted && 'text-gray-700',
						!isActive && !isCompleted && 'text-gray-500',
						!isActive && 'hidden sm:block'
					)}
				>
					{step.label}
				</span>
			</div>
		{/each}
	</div>
</div>
