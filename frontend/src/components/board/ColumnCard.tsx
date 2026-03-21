import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { BoardColumn } from '../../types/api';
import { TaskCard } from './TaskCard';

interface ColumnCardProps {
  column: BoardColumn;
  onAddTask: () => void;
}

export const ColumnCard = ({ column, onAddTask }: ColumnCardProps) => {
  const { setNodeRef, isOver } = useDroppable({ id: column.id, data: { columnId: column.id } });

  return (
    <div
      ref={setNodeRef}
      className="column-card"
      style={{ borderColor: isOver ? '#2563eb' : undefined, background: isOver ? '#f8fbff' : undefined }}
    >
      <div className="column-header">
        <div className="column-title">{column.name}</div>
        <button className="btn btn-secondary" onClick={onAddTask}>
          + Task
        </button>
      </div>

      <SortableContext items={column.tasks.map((task) => task.id)} strategy={verticalListSortingStrategy}>
        <div className="task-list">
          {column.tasks.map((task) => (
            <TaskCard key={task.id} task={task} columnId={column.id} />
          ))}
          {!column.tasks.length && <p className="helper-text">Drop a task or create a new one.</p>}
        </div>
      </SortableContext>
    </div>
  );
};
