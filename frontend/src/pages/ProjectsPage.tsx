import { Link, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useCreateProject, useProjects } from '../api/hooks';

interface ProjectForm {
  name: string;
  description?: string;
}

export const ProjectsPage = () => {
  const { organizationId } = useParams<{ organizationId: string }>();
  if (!organizationId) {
    return <p className="helper-text">Choose an organization to view its projects.</p>;
  }

  const { data: projects, isLoading, error } = useProjects(organizationId);
  const createProject = useCreateProject(organizationId!);
  const { register, handleSubmit, reset } = useForm<ProjectForm>();

  const onSubmit = async (values: ProjectForm) => {
    await createProject.mutateAsync(values);
    reset();
  };

  return (
    <div className="stack">
      <div className="page-header">
        <div>
          <h2>Projects</h2>
          <p className="helper-text">
            Projects belong to an organization. From here you can open a Kanban board or create a new project.
          </p>
        </div>
        <Link className="btn btn-secondary" to="/organizations">
          Back to orgs
        </Link>
      </div>

      <div className="card" style={{ maxWidth: 520 }}>
        <h3>Create project</h3>
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
        {createProject.isError && <p className="helper-text">Could not create project.</p>}
        <button className="btn btn-primary" type="submit" disabled={createProject.isPending}>
          {createProject.isPending ? 'Creating…' : 'Create project'}
        </button>
      </form>
    </div>

      {isLoading && <p>Loading projects…</p>}
      {error && <p className="helper-text">Unable to load projects.</p>}

      <div className="columns-grid">
        {projects?.map((project) => (
          <div key={project.id} className="card">
            <div className="column-header">
              <div>
                <div className="column-title">{project.name}</div>
                <p className="helper-text">{project.description || 'No description yet.'}</p>
              </div>
              <span className="badge">{project.status}</span>
            </div>
            <Link className="btn btn-secondary" to={`/projects/${project.id}/board`}>
              Open board
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
};
