import { Button } from '@/components/ui/Button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import { Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function Logistics() {
  const { t } = useTranslation('adminLogistics');

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t('page.title')}</h1>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          {t('page.action')}
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t('table.headers.provider')}</TableHead>
              <TableHead>{t('table.headers.serviceType')}</TableHead>
              <TableHead>{t('table.headers.regions')}</TableHead>
              <TableHead>{t('table.headers.status')}</TableHead>
              <TableHead className="text-right">{t('table.headers.actions')}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow>
              <TableCell colSpan={5} className="h-24 text-center">
                <p className="text-gray-500">{t('page.empty')}</p>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
