import { ActivityLog } from '../../types/api';

interface ActivityFeedProps {
  items?: ActivityLog[];
  isLoading?: boolean;
}

export const ActivityFeed = ({ items, isLoading }: ActivityFeedProps) => {
  if (isLoading) return <p className="helper-text">Loading activity…</p>;
  if (!items?.length) return <p className="helper-text">No activity yet.</p>;

  return (
    <div className="activity-panel">
      <h4>Activity</h4>
      {items.map((entry) => (
        <div key={entry.id} className="activity-item">
          <div className="inline" style={{ justifyContent: 'space-between' }}>
            <span className="badge">{entry.actionType}</span>
            <span className="helper-text">{new Date(entry.createdAt).toLocaleString()}</span>
          </div>
          <div className="column-title" style={{ fontSize: 15 }}>
            {entry.taskTitle}
          </div>
          <p className="helper-text">
            {entry.actorDisplayName || 'System'}: {entry.oldValue || '—'} → {entry.newValue || '—'}
          </p>
        </div>
      ))}
    </div>
  );
};
