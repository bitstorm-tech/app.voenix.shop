import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { UserData } from '../types';

interface UseUserDataFormReturn {
  formData: UserData;
  errors: Partial<Record<keyof UserData, string>>;
  handleChange: (field: keyof UserData, value: string) => void;
  validateForm: () => boolean;
  resetForm: () => void;
}

const initialFormData: UserData = {
  email: '',
  firstName: '',
  lastName: '',
  phoneNumber: '',
};

export function useUserDataForm(initialData?: UserData | null): UseUserDataFormReturn {
  const { t } = useTranslation('editor');
  const [formData, setFormData] = useState<UserData>(initialData || initialFormData);
  const [errors, setErrors] = useState<Partial<Record<keyof UserData, string>>>({});

  // Sync formData with initialData changes
  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    }
  }, [initialData]);

  const handleChange = (field: keyof UserData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof UserData, string>> = {};

    if (!formData.email) {
      newErrors.email = t('steps.userData.errors.emailRequired');
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = t('steps.userData.errors.emailInvalid');
    }

    if (formData.phoneNumber && !/^[\d\s\-+()]+$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = t('steps.userData.errors.phoneInvalid');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const resetForm = () => {
    setFormData(initialFormData);
    setErrors({});
  };

  return {
    formData,
    errors,
    handleChange,
    validateForm,
    resetForm,
  };
}
