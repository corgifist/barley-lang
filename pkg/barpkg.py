from github import Github, GithubException
import logging
import base64
import getpass
import os
import shutil
import pickle

g = Github("PERSONAL ACCESS TOKEN")
repo = g.get_user().get_repo("barley-package-archive")

def username():
    with open('data.pickle', 'rb') as f:
        return pickle.load(f)[0]

def ban_list():
    return base64.b64decode(repo.get_contents("ban_list.txt").content).decode('utf-8')

def ban(username):
    ban_sha = repo.get_contents("ban_list.txt").sha
    repo.update_file("ban_list.txt", "banning", ban_list() + "\n" + username, ban_sha, branch="main")

def ban_check(username):
    ban_list = base64.b64decode(repo.get_contents("ban_list.txt").content).decode('utf-8')
    if username in ban_list:
        print("Sorry, you can't use PKG. You was banned!")
        input()
        exit(1)

def consume_pickle():
    if not os.path.exists('data.pickle'):
        print("Please, do not remove data.pickle. It is contains very important information. Please restart")
        with open('data.pickle', 'w+') as pl:
            pl.write("")
        input()
        exit(1)

    with open('data.pickle') as f:
        pickle_auth() if f.read() == "" else ban_check(username())

def pickle_auth():
    print("\nBefore you will using PKG we need to auth you!\nexpressions now allowed\n")
    username = input("username ~~ ")
    ban_check(username)
    with open('data.pickle', 'wb+') as pck:
        pickle.dump([username], pck)
    

if not os.path.exists('pkgs'):
    os.mkdir('pkgs')

consume_pickle()


sudo = False

def upload(filename, name, description, deps):
    all_files = []
    contents = repo.get_contents("")
    while contents:
        file_content = contents.pop(0)
        if file_content.type == "dir":
            contents.extend(repo.get_contents(file_content.path))
        else:
            file = file_content
            all_files.append(str(file).replace('ContentFile(path="','').replace('")',''))

    with open(name + "/" + filename, 'r') as file:
        content = file.read()

    # Upload to github
    git_prefix = name + "/"
    git_file = git_prefix +  filename
    files_cont = repo.get_contents("current_state.txt")
    print(files_cont.content)
    if name in base64.b64decode(files_cont.content).decode('utf-8'):
        print("duplicate package '" + name + "'")
        return
    if git_file in all_files:
        contents = repo.get_contents(git_file)
        desc_contents = repo.get_contents(git_prefix + "description.txt")
        deps_contents = repo.get_contents(git_prefix + "deps.txt")
        files_contents = repo.get_contents("current_state.txt")
        repo.update_file(contents.path, "committing files", content, contents.sha, branch="main")
        repo.update_file(git_prefix + "description.txt", "commiting desription", description, desc_contents.sha, branch="main")
        repo.update_file(git_prefix + "deps.txt", "commiting deps", deps, deps_contents.sha, branch="main")
        repo.update_file("current_state.txt", "commiting files list", base64.b64decode(files_contents.content).decode('utf-8') + "\n" + name, files_contents.sha, branch="main")
        log_update(git_file)
    else:
        repo.create_file(git_file, "committing files", content, branch="main")
        repo.create_file(git_prefix + "description.txt", "committing description", description, branch="main")
        repo.create_file(git_prefix + "deps.txt", "commiting deps", deps, branch="main")
        files_contents = repo.get_contents("current_state.txt")
        repo.update_file("current_state.txt", "commiting files list", base64.b64decode(files_contents.content).decode('utf-8') + "\n" + name, files_contents.sha, branch="main")
        log_created(git_file)


def upload_files(files, name, description, deps):
    for iter, file in enumerate(files):
        consume_pickle()
        print(f'Done {iter} files, max: {len(files)}')
        upload(file, name, description, deps)

def log_created(f):
    print(f"[OK] {f} CREATED")

def log_update(f):
    print(f"[OK] {f} UPDATED")

def prompt_expr(inp):
    consume_pickle()
    pts = inp.split(" ")
    text = " ".join(pts[1::])
    consume_pickle()
    if (pts[0] == "file"):
        consume_pickle()
        return open(text).read()
    elif (pts[0] == "raw"):
        consume_pickle()
        return text
    elif (pts[0] == "fc"):
        consume_pickle()
        text_parts = text.split(" ")
        try:
            consume_pickle()
            return single_file(text_parts[1], text_parts[2])
        except IndexError:
            consume_pickle()
            return dir_file(text)
    elif pts[0] == "list":
        consume_pickle()
        return str(pts[1::])
    elif pts[0] == "lst_loop":
        acc = []
        while True:
            consume_pickle()
            prompt = input("~~ ")
            consume_pickle()
            parts = prompt.split(" ")
            if parts[0] == "LIST_BREAK":
                consume_pickle()
                break
            elif parts[0] == "LIST_INSERT":
                consume_pickle()
                index = int(input(" >~ "))
                acc[index] = input(">>> ")
                continue
            elif parts[0] == "LIST_CLEAR":
                consume_pickle()
                acc = []
                continue
            elif parts[0] == "LIST_SWAP":
                consume_pickle()
                b = acc[len(acc) - 1]
                c = acc[len(acc) - 2]
                acc[len(acc) - 2] = b
                acc[len(acc) - 1] = c
                consume_pickle()
                continue
            elif parts[0] == "LIST_DUMP":
                consume_pickle()
                print("dump: " + str(acc))
                consume_pickle()
                continue
            acc.append(prompt_expr(prompt))

        return str(acc)
    elif pts[0] == "in_sudo":
        return str(sudo)
    elif pts[0] == "ban_list":
        return ban_list()

    print("bad expression: '" + inp + "'")
    return ""

#########################

def get_sha_for_tag(tag):
    """
    Returns a commit PyGithub object for the specified repository and tag.
    """
    consume_pickle()
    branches = repo.get_branches()
    matched_branches = [match for match in branches if match.name == tag]
    if matched_branches:
        consume_pickle()
        return matched_branches[0].commit.sha

    tags = repo.get_tags()
    matched_tags = [match for match in tags if match.name == tag]
    consume_pickle()
    if not matched_tags:
        raise ValueError('No Tag or Branch exists with that name')
    return matched_tags[0].commit.sha


def download_directory(repository, sha, server_path):
    """
    Download all contents at server_path with commit tag sha in
    the repository.
    """
    contents = repository.get_contents(server_path, ref=sha)
    consume_pickle()

    for content in contents:
        print("Processing %s" % content.path)
        try:
            consume_pickle()
            path = content.path
            parent = makedirs(path)
            try:
                consume_pickle()
                os.makedirs(parent)
            except OSError:
                pass
            file_content = repository.get_contents(path, ref=sha)
            file_data = base64.b64decode(file_content.content)
            file_out = open(path, "w+")
            file_out.write(file_data.decode('utf-8'))
            file_out.close()
            consume_pickle()
        except (GithubException, IOError) as exc:
            consume_pickle()
            logging.error('Error processing %s: %s', content.path, exc)

    try:
        if not os.path.exists("pkgs"):
            os.mkdir("pkgs")
        consume_pickle()
        shutil.move(server_path, "pkgs")
    except OSError as ex:
        consume_pickle()
        print(ex)


def makedirs(path):
    parts = path.split("/")
    return "/".join(parts[0:len(parts) - 1])

#########################

def upload_interact():
    consume_pickle()
    print("\nWelcome to uploader!\n")
    name = prompt_expr(input("name ~~ "))
    consume_pickle()
    deps = prompt_expr(input("deps ~~ "))
    consume_pickle()
    description = prompt_expr(input("desc ~~ "))
    consume_pickle()
    list_expr = eval(prompt_expr(input(">~ ")))
    consume_pickle()
    upload_files(list_expr, name, description, deps)
 

def interact_sudo():
    consume_pickle()
    print("Welcome to interactive sudo!")
    print("What do you want to do?")
    prompt = input(">>> ")
    parts = prompt.split(" ")
    while True:
        if parts[0] == "ban":
            consume_pickle()
            ban(parts[1])
        elif parts[0] == "exit":
            consume_pickle()
            break
    consume_pickle()
    

def enter_sudo():
    global sudo
    print("You are trying to enter sudo mode!")
    print("In sudo mode you can delete packages")
    print("Only corgifist (creator of Barley and PKG manager) knows sudo password")
    print("Password is not echoed")
    consume_pickle()
    while True:
        prompt = getpass.getpass("!! ")
        if prompt == "exit": break
        sudo = prompt == "busa_cat_666_omlet_112"
        if sudo:
            print("Success!")
            break

def download(module):
    try:
        consume_pickle()
        download_directory(repo, get_sha_for_tag("main"), module)
        consume_pickle()
        print("success '" + module + "'")
        consume_pickle()
    except GithubException as ex:
        print(ex)

def desc(package):
    desc = base64.b64decode(repo.get_contents(f"{package}/description.txt").content).decode('utf-8')
    print(desc)

def deps(package):
    deps = base64.b64decode(repo.get_contents(f"{package}/deps.txt").content).decode('utf-8')
    dps = deps.split("\n")
    for dep in dps:
        if dep == "none":   continue
        download(dep)
        

def interpret(parts):
    if (parts[0] == "upload"):
        consume_pickle()
        upload_interact()
    elif (parts[0] == "exit"):
        consume_pickle()
        exit(1)
    elif (parts[0] == "sudo"):
        consume_pickle()
        enter_sudo() if not sudo else interact_sudo()
    elif (parts[0] == "install"):
        consume_pickle()
        download(parts[1])
    elif (parts[0] == "destroy"):
        os.remove('data.pickle')
    elif (parts[0] == "desc"):
        desc(parts[1])
    elif (parts[0] == "deps"):
        deps(parts[1])
    else:
        consume_pickle()
        print(prompt_expr(" ".join(parts)))
            

def single_file(dr, n):
    contents = repo.get_contents(f'{dr}/{n}')
    return base64.b64decode(contents.content).decode('utf-8')

def dir_file(dr):
    contents = repo.get_contents(f'{dr}')
    return base64.b64decode(contents.content).decode('utf-8')

def update_progress(progress):
    print('\r[{0}] {1}%'.format('#'*(progress/10), progress))


print("Barley Package Manager for Barley beta3")
print(f"Hello, {username()}!") 

while True:
    consume_pickle()
    prompt = input(">> ")
    parts = prompt.split(" ")
    consume_pickle()
    interpret(parts)
        
