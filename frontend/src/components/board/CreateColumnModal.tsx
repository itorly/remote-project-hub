import { useForm } from 'react-hook-form';
import { CreateColumnInput } from '../../types/api';

interface CreateColumnModalProps {
  onSubmit: (values: CreateColumnInput) => Promise<void> | void;
  onClose: () => void;
}

export const CreateColumnModal = ({ onSubmit, onClose }: CreateColumnModalProps) => {
  const { register, handleSubmit, reset } = useForm<CreateColumnInput>();

  const handleSave = async (values: CreateColumnInput) => {
    await onSubmit(values);
    reset();
    onClose();
  };

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h3>Create column</h3>
        <form onSubmit={handleSubmit(handleSave)} className="stack">
          <div className="form-group">
            <label className="label" htmlFor="name">
              Name
            </label>
            <input id="name" className="input" {...register('name', { required: true })} />
          </div>
          <button className="btn btn-primary" type="submit">
            Create
          </button>
          <button className="btn btn-ghost" type="button" onClick={onClose}>
            Cancel
          </button>
        </form>
      </div>
    </div>
  );
};
