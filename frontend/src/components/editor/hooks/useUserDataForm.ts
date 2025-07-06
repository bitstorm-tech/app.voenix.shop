import { useState } from 'react';
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
  const [formData, setFormData] = useState<UserData>(initialData || initialFormData);
  const [errors, setErrors] = useState<Partial<Record<keyof UserData, string>>>({});

  const handleChange = (field: keyof UserData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof UserData, string>> = {};

    if (!formData.email) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }

    if (formData.phoneNumber && !/^[\d\s\-+()]+$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = 'Please enter a valid phone number';
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
