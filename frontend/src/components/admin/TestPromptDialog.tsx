import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/Dialog';
import { useTranslation } from 'react-i18next';

interface TestPromptDialogProps {
  isOpen: boolean;
  testingPromptId: number | undefined;
  onClose: () => void;
}

export default function TestPromptDialog({ isOpen, onClose }: TestPromptDialogProps) {
  const { t } = useTranslation('adminPrompts');

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t('modal.title')}</DialogTitle>
        </DialogHeader>
        {/*<ImagePicker defaultPromptId={_testingPromptId} storeImages={false} />*/}
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            {t('modal.close')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
