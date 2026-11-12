lost_found
==========

Installs Java and deploys the Lost and Found Spring Boot jar as a systemd service under a dedicated system user.

Requirements
------------

- Ubuntu/Debian target with systemd and sudo.
- The jar built beforehand (`./mvnw clean package`) at `target/lost-found.jar`.

Role Variables
--------------

Defined in `defaults/main.yml`:

| Variable | Default | Meaning |
|----------|---------|---------|
| `app_name` | `lost-found` | systemd service name |
| `app_port` | `8090` | Passed to the app as `PORT` |
| `app_user` | `lostfound` | System user running the service |
| `app_dir` | `/opt/lost-found` | Install directory |
| `jar_src` | `{{ playbook_dir }}/../target/lost-found.jar` | Jar on the control machine |
| `java_package` | `openjdk-17-jre-headless` | apt package |

Dependencies
------------

None.

Example Playbook
----------------

    - hosts: lost_found
      become: true
      roles:
        - lost_found

License
-------

MIT
