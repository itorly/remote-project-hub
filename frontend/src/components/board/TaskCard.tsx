import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Task } from '../../types/api';

interface TaskCardProps {
  task: Task;
  columnId: number;
}

export const TaskCard = ({ task, columnId }: TaskCardProps) => {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: task.id,
    data: { columnId }
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.7 : 1,
    borderColor: isDragging ? '#93c5fd' : undefined
  };

  return (
    <div ref={setNodeRef} style={style} className="task-card" {...attributes} {...listeners}>
      <div className="column-title" style={{ fontSize: 15 }}>
        {task.title}
      </div>
      {task.description && <p className="helper-text">{task.description}</p>}
      <div className="inline" style={{ justifyContent: 'space-between' }}>
        {task.tags && <span className="badge">{task.tags}</span>}
        {task.assigneeDisplayName && <span className="helper-text">@{task.assigneeDisplayName}</span>}
      </div>
    </div>
  );
};
