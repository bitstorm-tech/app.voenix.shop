export type ClassValue = string | false | null | undefined | ClassValue[];

function collectClasses(value: ClassValue, classes: string[]) {
	if (!value) {
		return;
	}

	if (Array.isArray(value)) {
		for (const entry of value) {
			collectClasses(entry, classes);
		}
		return;
	}

	if (typeof value === 'string') {
		classes.push(value);
	}
}

export function cn(...inputs: ClassValue[]): string {
	const classes: string[] = [];
	for (const value of inputs) {
		collectClasses(value, classes);
	}
	return classes.join(' ');
}
