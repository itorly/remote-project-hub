import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { BoardColumn, CreateTaskInput } from '../../types/api';

interface CreateTaskModalProps {
  columns: BoardColumn[];
  defaultColumnId?: number;
  onSubmit: (values: CreateTaskInput) => Promise<void> | void;
  onClose: () => void;
}

export const CreateTaskModal = ({ columns, defaultColumnId, onSubmit, onClose }: CreateTaskModalProps) => {
  const { register, handleSubmit, reset } = useForm<CreateTaskInput>({
    defaultValues: { columnId: defaultColumnId, title: '', description: '', tags: '' }
  });

  useEffect(() => {
    const fallback = defaultColumnId ?? columns[0]?.id;
    reset({ columnId: fallback, title: '', description: '', tags: '' });
  }, [defaultColumnId, columns, reset]);

  const handleSave = async (values: CreateTaskInput) => {
    await onSubmit({
      ...values,
      columnId: Number(values.columnId),
      assigneeId: values.assigneeId ? Number(values.assigneeId) : undefined
    });
    onClose();
  };

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h3>Create task</h3>
        <form onSubmit={handleSubmit(handleSave)} className="stack">
          <div className="form-group">
            <label className="label" htmlFor="columnId">
              Column
            </label>
            <select id="columnId" className="select" {...register('columnId', { required: true })}>
              {columns.map((col) => (
                <option key={col.id} value={col.id}>
                  {col.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="label" htmlFor="title">
              Title
            </label>
            <input id="title" className="input" {...register('title', { required: true })} />
          </div>
          <div className="form-group">
            <label className="label" htmlFor="description">
              Description
            </label>
            <textarea id="description" className="textarea" rows={3} {...register('description')} />
          </div>
          <div className="form-group">
            <label className="label" htmlFor="tags">
              Tags (comma separated)
            </label>
            <input id="tags" className="input" placeholder="frontend, api" {...register('tags')} />
          </div>
          <button className="btn btn-primary" type="submit">
            Save task
          </button>
          <button className="btn btn-ghost" type="button" onClick={onClose}>
            Cancel
          </button>
        </form>
      </div>
    </div>
  );
};
