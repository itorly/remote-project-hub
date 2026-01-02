import { render, screen } from '@testing-library/react';
import { DndContext } from '@dnd-kit/core';
import { TaskCard } from '../src/components/board/TaskCard';
import { Task } from '../src/types/api';

describe('TaskCard', () => {
  it('renders task title and tags', () => {
    const task: Task = {
      id: 1,
      columnId: 2,
      title: 'Design header',
      description: 'Update styles',
      status: 'TODO',
      assigneeDisplayName: 'Ava',
      assigneeId: 5,
      dueDate: null,
      tags: 'frontend'
    };

    render(
      <DndContext>
        <TaskCard task={task} columnId={2} />
      </DndContext>
    );

    expect(screen.getByText('Design header')).toBeInTheDocument();
    expect(screen.getByText('frontend')).toBeInTheDocument();
    expect(screen.getByText('@Ava')).toBeInTheDocument();
  });
});
