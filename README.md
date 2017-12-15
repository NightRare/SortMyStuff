# SortMyStuff

## The Application
SortMyStuff is a handy mobile app that helps you to keep track of your assets or belongings effortless.

[![IMAGE ALT TEXT HERE](https://i.imgur.com/FTNRc4i.png)](https://www.youtube.com/watch?v=GI7K3f0QMpM)


### Features

- Organise assets and belongings in a tree-like structure interface
- Auto-naming items by recognising the photos
- Move assets around to reflect it's physical placement
- Search through your own inventory
- Keep handy product information records along with photos
- Synchronise your data across devices
- Designed to hold hundreds or more entries

### Photo recognition

Digitalising is tiring. That's why we have photo recognition service to simplify it for you. Photo recognition service can rename all the default-named assets automatically according to the content of its customised photo. This feature is embedded in the app as a background service which can be launched with one click. While the service is running, it is safe to alter the state of *any* asset, including move, delete or change its name and details.

### Path bar

The path bar is an interactive UI component with which not only allows you to see the position of the asset with a glance, but also makes the navigation on a mobile device more intuitive (in comparison with using a tree of nodes that can be expanded and collapsed).

![](https://i.imgur.com/PoDG7JK.gif)

### Stability and Memory Use

SortMyStuff can manage more than hundreds of entries. A poorly managed app with a large amount of images can easily lag or even crash. But SortMyStuff is better than that — unnecessary Bitmaps are recycled properly, memory leak is checked, cache limit is set to prevent consuming too much memory. In other words, SortMyStuff is light, fast, reliable yet powerful.

### App Architecture and Data process

The architecture of SortMyStuff is MVP, where I make sure the Presenters are fully decoupled from the Android Framework and the data layer (Model) so that they can all be easily modified, extended and tested in their own scope. 

The data layer of SortMyStuff is currently powered by Firebase (Authentication, Realtime Database and Storage) . All the user data is stored remotely on Firebase yet a local cache is enabled for offline use. In the future, the data service may be migrated from Firebase to a dedicated [SortMyStuff REST API](https://github.com/NightRare/SortMyStuffAPI), but this won't happen recently.

In addition, SortMyStuff heavily depends on RxJava, which has greatly improved the readability and maintainability of the code.


## Dependencies

- [RxJava](https://github.com/ReactiveX/RxJava/tree/1.x)
- Firebase
- [Dagger 2](https://github.com/google/dagger)
- [Gson](https://github.com/google/gson)
- [Retrofit](https://github.com/square/retrofit)
- [CloudSight (API)](https://cloudsight.ai/)
- [RxFirebase](https://github.com/nmoskalenko/RxFirebase)
- The photo taking feature is dependent on [SquareCamera by boxme](https://github.com/boxme/SquareCamera.git)


## The Project

The project is now [soley maintained and extended by Yuan](https://github.com/NightRare/SortMyStuff/graphs/contributors) after tag [a0.2](https://github.com/NightRare/SortMyStuff/tree/a0.2). 

The original project was created as the culmination of our work for the paper Software Development Practice throughout Semester 1, 2017 at Auckland University of Technology. The aim of the project was for us to be comfortable with collaboration in an Agile development environment using the Scrum framework. 

Original team members

- Donna
- Jing
- Yuan

## Declaration of the use of resources

All the photos (../app/src/main/assets/images/demo/) of the demo assets were retrieved from the internet. They are used only for the purpose of demonstrating the use of the application and are not necessarily parts of the application. These images is only used for this non-commercial project on educational purpose.

| Image                                    | Retrieved from                           |
| ---------------------------------------- | ---------------------------------------- |
| StudyRoom.png, BookshelfPhilosophy.png, BookshelfLiterature.png | http://jiaju.sina.com.cn/news/fengshui/20140919/380782.shtml |
| Bedroom.png                              | http://www.quanjing.com/imgbuy/fod-00722981.html |
| Office.png                               | https://www.vcg.com/creative/809042572   |
| Kindle.png                               | https://zhongce.sina.com.cn/report/view/2244 |
