import {
  DndContext,
  DragEndEvent,
  PointerSensor,
  closestCorners,
  useSensor,
  useSensors
} from '@dnd-kit/core';
import { BoardColumn } from '../../types/api';
import { ColumnCard } from './ColumnCard';

interface ColumnListProps {
  columns: BoardColumn[];
  onAddTask: (column: BoardColumn) => void;
  onTaskDrop: (taskId: number, targetColumnId: number) => void;
}

export const ColumnList = ({ columns, onAddTask, onTaskDrop }: ColumnListProps) => {
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }));

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over) return;

    const taskId = Number(active.id);
    const fromColumn = active.data.current?.columnId as number | undefined;
    const targetColumn = (over.data.current?.columnId as number | undefined) ?? Number(over.id);

    if (fromColumn && targetColumn && fromColumn !== targetColumn) {
      onTaskDrop(taskId, targetColumn);
    }
  };

  return (
    <DndContext sensors={sensors} collisionDetection={closestCorners} onDragEnd={handleDragEnd}>
      <div className="columns-grid">
        {columns.map((column) => (
          <ColumnCard key={column.id} column={column} onAddTask={() => onAddTask(column)} />
        ))}
      </div>
    </DndContext>
  );
};
