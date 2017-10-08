Deployment
====

## prerequisits
	$ ansible-galaxy install -r requirements.yml

## Run Ansible Playbook against Vagrant box

    ansible-playbook -i inventories/vagrant playbook.yml

## Run Ansible Playbook against Development Stage VM (requires sudo)

    ansible-playbook -i inventories/hosts -l dev playbook.yml
