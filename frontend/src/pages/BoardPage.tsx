import { useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { ColumnList } from '../components/board/ColumnList';
import { CreateColumnModal } from '../components/board/CreateColumnModal';
import { CreateTaskModal } from '../components/board/CreateTaskModal';
import { ActivityFeed } from '../components/activity/ActivityFeed';
import { useActivity, useBoard, useCreateColumn, useCreateTask, useMoveTask } from '../api/hooks';
import { BoardColumn, CreateTaskInput } from '../types/api';

export const BoardPage = () => {
  const { projectId } = useParams<{ projectId: string }>();
  if (!projectId) {
    return <p className="helper-text">Missing project id in URL.</p>;
  }

  const { data: board, isLoading, error } = useBoard(projectId);
  const { data: activity, isLoading: isActivityLoading } = useActivity(projectId);
  const createColumn = useCreateColumn(projectId!);
  const createTask = useCreateTask(projectId!);
  const moveTask = useMoveTask(projectId!);

  const [isColumnModalOpen, setIsColumnModalOpen] = useState(false);
  const [taskModalColumn, setTaskModalColumn] = useState<BoardColumn | null>(null);

  const sortedColumns = useMemo(() => {
    return (board?.columns || []).slice().sort((a, b) => a.position - b.position);
  }, [board]);

  const handleTaskDrop = (taskId: number, targetColumnId: number) => {
    moveTask.mutate({ taskId, targetColumnId });
  };

  const handleTaskCreate = async (values: CreateTaskInput) => {
    await createTask.mutateAsync(values);
  };

  const handleColumnCreate = async (values: { name: string }) => {
    await createColumn.mutateAsync(values);
  };

  if (isLoading) return <p>Loading board…</p>;
  if (error) return <p className="helper-text">Unable to load board.</p>;
  if (!board) return <p className="helper-text">Board not found.</p>;

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h2>{board?.projectName || 'Board'}</h2>
          <p className="helper-text">
            Drag tasks across columns to sync with the backend move endpoint. Create new columns and tasks inline.
          </p>
        </div>
        <div className="inline" style={{ gap: 8 }}>
          <button className="btn btn-secondary" onClick={() => setTaskModalColumn(sortedColumns[0] ?? null)}>
            + Task
          </button>
          <button className="btn btn-primary" onClick={() => setIsColumnModalOpen(true)}>
            + Column
          </button>
        </div>
      </div>

      <div className="grid-two">
        <div className="stack">
          {!sortedColumns.length && (
            <div className="card">
              <p className="helper-text">No columns yet. Create one to start organizing tasks.</p>
            </div>
          )}
          <ColumnList
            columns={sortedColumns}
            onAddTask={(column) => setTaskModalColumn(column)}
            onTaskDrop={handleTaskDrop}
          />
        </div>
        <ActivityFeed items={activity} isLoading={isActivityLoading} />
      </div>

      {isColumnModalOpen && (
        <CreateColumnModal onSubmit={handleColumnCreate} onClose={() => setIsColumnModalOpen(false)} />
      )}
      {taskModalColumn && (
        <CreateTaskModal
          columns={sortedColumns}
          defaultColumnId={taskModalColumn.id}
          onClose={() => setTaskModalColumn(null)}
          onSubmit={handleTaskCreate}
        />
      )}
    </div>
  );
};
