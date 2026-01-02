import { render, screen } from '@testing-library/react';
import { ActivityFeed } from '../src/components/activity/ActivityFeed';
import { ActivityLog } from '../src/types/api';

describe('ActivityFeed', () => {
  it('shows empty state', () => {
    render(<ActivityFeed items={[]} />);
    expect(screen.getByText(/No activity yet/i)).toBeInTheDocument();
  });

  it('renders an activity entry', () => {
    const item: ActivityLog = {
      id: 10,
      projectId: 2,
      taskId: 3,
      taskTitle: 'Design header',
      actionType: 'TASK_MOVED',
      oldValue: 'Todo',
      newValue: 'In Progress',
      actorId: 1,
      actorDisplayName: 'Ava',
      createdAt: '2024-01-01T00:00:00Z'
    };

    render(<ActivityFeed items={[item]} />);

    expect(screen.getByText('Design header')).toBeInTheDocument();
    expect(screen.getByText(/TASK_MOVED/i)).toBeInTheDocument();
    expect(screen.getByText(/Ava/)).toBeInTheDocument();
  });
});
