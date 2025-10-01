<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { HTMLAnchorAttributes, HTMLButtonAttributes } from 'svelte/elements';
	import type { ButtonSize, ButtonVariant } from './styles';
	import { buttonVariants } from './styles';

	type CommonProps = {
		variant?: ButtonVariant;
		size?: ButtonSize;
		class?: string;
		href?: string;
		type?: HTMLButtonElement['type'];
		children?: Snippet;
	};

	type AnchorExtras = Omit<HTMLAnchorAttributes, keyof CommonProps>;
	type ButtonExtras = Omit<HTMLButtonAttributes, keyof CommonProps>;

	let {
		variant = 'default',
		size = 'default',
		class: className = '',
		href,
		type = 'button',
		children,
		...attrs
	}: CommonProps & AnchorExtras & ButtonExtras = $props();

	const classes = $derived(buttonVariants({ variant, size, class: className }));
</script>

{#if href}
	<a class={classes} {href} {...attrs}>
		{@render children?.()}
	</a>
{:else}
	<button class={classes} {type} {...attrs}>
		{@render children?.()}
	</button>
{/if}
