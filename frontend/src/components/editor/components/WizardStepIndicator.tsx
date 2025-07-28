import { cn } from '@/lib/utils';
import { useWizardStore } from '@/stores/editor/useWizardStore';
import { Check } from 'lucide-react';
import { STEP_INDEX, STEP_LABELS, WIZARD_STEPS } from '../constants';

export default function WizardStepIndicator() {
  const currentStep = useWizardStore((state) => state.currentStep);
  const getCompletedSteps = useWizardStore((state) => state.getCompletedSteps);
  const completedSteps = getCompletedSteps();
  const currentStepIndex = STEP_INDEX[currentStep];

  return (
    <div className="relative">
      <div className="absolute top-5 left-0 h-0.5 w-full bg-gray-200">
        <div
          className="bg-primary h-full transition-all duration-300"
          style={{
            width: `${(currentStepIndex / (WIZARD_STEPS.length - 1)) * 100}%`,
          }}
        />
      </div>

      <div className="relative flex justify-between">
        {WIZARD_STEPS.map((step, index) => {
          const isActive = step === currentStep;
          const isCompleted = completedSteps.includes(step) || index < currentStepIndex;

          return (
            <div key={step} className="flex flex-col items-center">
              <div
                className={cn(
                  'relative z-10 flex h-10 w-10 items-center justify-center rounded-full border-2 bg-white transition-all',
                  isActive && 'border-primary bg-primary text-white',
                  isCompleted && !isActive && 'border-primary bg-primary text-white',
                  !isActive && !isCompleted && 'border-gray-300 text-gray-500',
                )}
              >
                {isCompleted && !isActive ? <Check className="h-5 w-5" /> : <span className="text-sm font-semibold">{index + 1}</span>}
              </div>
              <span
                className={cn(
                  'mt-2 text-xs font-medium',
                  isActive && 'text-primary',
                  !isActive && isCompleted && 'text-gray-700',
                  !isActive && !isCompleted && 'text-gray-500',
                  // Show labels for all steps on desktop, but only current step on mobile
                  !isActive && 'hidden sm:block',
                )}
              >
                {STEP_LABELS[step]}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
