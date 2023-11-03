
"""Supply version and commit hash info."""
import os

from admin_api.version import __version__


def _get_build_openshift_commit_hash():
    return os.getenv('OPENSHIFT_BUILD_COMMIT', None)


def get_run_version():
    """Return a formatted version string for this service."""
    commit_hash = _get_build_openshift_commit_hash()
    if commit_hash:
        return f'{__version__}-{commit_hash}'
    return __version__
