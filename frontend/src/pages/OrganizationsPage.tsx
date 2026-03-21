import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useCreateOrganization, useOrganizations } from '../api/hooks';

interface OrganizationForm {
  name: string;
  description?: string;
}

export const OrganizationsPage = () => {
  const { data, isLoading, error } = useOrganizations();
  const { register, handleSubmit, reset } = useForm<OrganizationForm>();
  const createOrg = useCreateOrganization();

  const onSubmit = async (values: OrganizationForm) => {
    await createOrg.mutateAsync(values);
    reset();
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h2>Your organizations</h2>
          <p className="helper-text">Pick an organization to view its projects or spin up a new one.</p>
        </div>
      </div>

      <div className="card" style={{ maxWidth: 520 }}>
        <h3>Create organization</h3>
        <form onSubmit={handleSubmit(onSubmit)} className="stack">
          <div className="form-group">
            <label className="label" htmlFor="name">
              Name
            </label>
            <input id="name" className="input" {...register('name', { required: true })} />
          </div>
          <div className="form-group">
            <label className="label" htmlFor="description">
              Description
            </label>
            <textarea id="description" className="textarea" rows={3} {...register('description')} />
          </div>
          {createOrg.isError && <p className="helper-text">Unable to create organization.</p>}
          <button className="btn btn-primary" type="submit" disabled={createOrg.isPending}>
            {createOrg.isPending ? 'Creating…' : 'Create organization'}
          </button>
        </form>
        <p className="helper-text">
          Organizations own projects. Membership roles from the API flow through to project access.
        </p>
      </div>

      {isLoading && <p>Loading organizations…</p>}
      {error && <p className="helper-text">Failed to load organizations</p>}

      <div className="columns-grid">
        {data?.map((org) => (
          <div key={org.id} className="card">
            <div className="column-header">
              <div>
                <div className="column-title">{org.name}</div>
                <p className="helper-text">{org.description || 'No description yet.'}</p>
              </div>
              <span className="badge">{org.role}</span>
            </div>
            <Link className="btn btn-secondary" to={`/organizations/${org.id}/projects`}>
              View projects
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
};
