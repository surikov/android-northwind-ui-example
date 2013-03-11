package northwind.ui.example;

import reactive.ui.*;
import android.net.Uri;
import android.os.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import tee.binding.*;
import tee.binding.it.*;
import tee.binding.task.*;

public class ActivityShowOrders extends Activity {
	Layoutless layoutless;
	ColumnText columnOrderID = new ColumnText();
	ColumnDescription columnOrderDate = new ColumnDescription();
	ColumnDescription columnShipDate = new ColumnDescription();
	ColumnText columnDoneDate = new ColumnText();
	ColumnText columnCustomer = new ColumnText();
	ColumnText columnFreight = new ColumnText();
	ColumnDescription columnMenu = new ColumnDescription();
	ColumnBitmap columnIcon = new ColumnBitmap();
	DataGrid orders;
	Toggle shippedOnly = new Toggle();
	Numeric ordersOffset = new Numeric().value(0);
	int pageSize = 25;
	Task newOrder = new Task() {
		@Override
		public void doTask() {
			Intent intent = new Intent();
			intent.setClass(ActivityShowOrders.this, ActivityEditOrder.class);
			ActivityShowOrders.this.startActivityForResult(intent, 0);
		}
	};
	Task filter = new Task() {
		@Override
		public void doTask() {
			shippedOnly.value(!shippedOnly.value());
			refresh.start(ActivityShowOrders.this);
		}
	};
	Task about = new Task() {
		@Override
		public void doTask() {
			String link="https://code.google.com/p/android-northwind-ui-example/";
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			startActivity(browserIntent);
		}
	};
	Expect refresh = new Expect().status.is("Wait...").task.is(new Task() {
		@Override
		public void doTask() {
			Tools.initDB(ActivityShowOrders.this);
			orders.clearColumns();
			requery();
		}
	})//
	.afterDone.is(new Task() {
		@Override
		public void doTask() {
			orders.refresh();
		}
	});

	void requery() {
		String sql = "select"// 
				+ " 		Orders.rowid,Orders.OrderID,Orders.OrderDate,Orders.RequiredDate,Orders.ShippedDate,Orders.Freight"//
				+ " 		,Customers.CompanyName as Customer,Shippers.CompanyName as Shipper,Employees.FirstName,Employees.LastName"//
				+ " 	from Orders"//
				+ " 		join Customers on Customers.CustomerID=Orders.CustomerID"//
				+ " 		join Shippers on Shippers.ShipperID=Orders.ShipVia"//
				+ " 		join Employees on Employees.EmployeeID=Orders.EmployeeID";
		if (shippedOnly.value()) {
			sql = sql + "	where ifnull(shippedDate,'')=''";
		}
		sql = sql + " 	order by Orders.OrderDate desc,Orders.OrderID desc"//
				+ " limit " + (pageSize * 3) + " offset " + ordersOffset.value().intValue();
		Bough data = Auxiliary.fromCursor(Tools.db(this).rawQuery(sql, null), true);
		//System.out.println("requery done " + data.children.size());
		for (int i = 0; i < data.children.size(); i++) {
			Bough row = data.children.get(i);
			final String orderID = row.child("OrderID").value.property.value();
			Task tapOrder = new Task() {
				@Override
				public void doTask() {
					tapOrder(orderID);
				}
			};
			columnOrderID.cell(orderID, tapOrder);
			String orderDate = Tools.formatDate(Auxiliary.date(row.child("OrderDate").value.property.value()));
			String shipDate = Tools.formatDate(Auxiliary.date(row.child("RequiredDate").value.property.value()));
			String doneDate = Tools.formatDate(Auxiliary.date(row.child("ShippedDate").value.property.value()));
			columnOrderDate.cell(orderDate, tapOrder, row.child("FirstName").value.property.value() + " " + row.child("LastName").value.property.value());
			columnShipDate.cell(shipDate, tapOrder, row.child("Shipper").value.property.value());
			columnDoneDate.cell(doneDate, tapOrder);
			columnCustomer.cell(row.child("Customer").value.property.value(), tapOrder);
			columnFreight.cell(row.child("Freight").value.property.value(), tapOrder);
		}
	}
	void tapOrder(String orderID) {
		Intent intent = new Intent();
		intent.setClass(this, ActivityEditOrder.class);
		intent.putExtra("id", orderID);
		this.startActivityForResult(intent, 0);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		refresh.start(this);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("Northwind UI Example - Orders");
		layoutless = new Layoutless(this);
		setContentView(layoutless);
		compose();
		refresh.start(this);
	}
	void compose() {
		columnMenu//
				.cell("New order", newOrder, "Create new order")//
				.cell("Unshipped orders only", filter, "Show/hide shipped orders")//
				.cell("About", about, "About this application...")//
		;
		columnIcon//
				.cell(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.add), Auxiliary.tapSize, Auxiliary.tapSize, true))//
				.cell(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.filter), Auxiliary.tapSize, Auxiliary.tapSize, true))//
				.cell(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.info), Auxiliary.tapSize, Auxiliary.tapSize, true))//
		;
		Numeric menuSplit = new Numeric().value(0.8 * Auxiliary.screenWidth(this));
		layoutless.child(new Decor(this)//
				.background.is(Auxiliary.colorBackground)//
						.sketch(new SketchPlate().tint(new TintBitmapTile()//
								.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes3))//
								)//
						.width.is(layoutless.width().property)//
						.height.is(layoutless.height().property)//
						)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.labelText.is("Orders")//
				.labelSize.is(0.2 * Auxiliary.screenWidth(this))//
				//.labelColor.is(0x11006699)//
				.labelColor.is(0x22006699)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes4)).top().is(layoutless.height().property.minus(250)).width().is(400)//
						.height().is(250)//
				);
		orders = new DataGrid(this);
		layoutless.child(orders//
				.center.is(true)//
				.headerHeight.is(0.5 * Auxiliary.tapSize)//
				.dataOffset.is(ordersOffset)//
				.beforeFlip.is(new Task() {
					@Override
					public void doTask() {
						requery();
					}
				})//
				.pageSize.is(pageSize)//
						.columns(new Column[] { columnOrderID.title.is("ID").width.is(1.5 * Auxiliary.tapSize)//
								, columnCustomer.title.is("Customer").width.is(3 * Auxiliary.tapSize)//
								, columnShipDate.title.is("Ship").width.is(2.5 * Auxiliary.tapSize)//
								, columnDoneDate.title.is("Done").width.is(2.5 * Auxiliary.tapSize)//
								, columnFreight.title.is("Freight").width.is(1.5 * Auxiliary.tapSize) //
								, columnOrderDate.title.is("Date").width.is(2.5 * Auxiliary.tapSize) //
								})//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.background.is(Auxiliary.colorBackground)//
						.sketch(new SketchPlate().tint(new TintBitmapTile()//
								.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflakes3))//
								)//
						.width.is(layoutless.width().property)//
						.height.is(layoutless.height().property)//
						)//
						.left().is(menuSplit)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		layoutless.child(new Decor(this)//
				.bitmap.is(BitmapFactory.decodeResource(getResources(), R.drawable.snowflake2)).left().is(menuSplit).width().is(322)//
						.height().is(406)//
				);
		//final DataGrid ordersGrid=;
		layoutless.child(new SplitLeftRight(this)//
				.split.is(menuSplit)//
						.rightSide(new DataGrid(this)//
								.noHead.is(true)//
										.columns(new Column[] {//
												columnIcon.title.is("Icon").width.is(Auxiliary.tapSize).noVerticalBorder.is(true).noHorizontalBorder.is(true) //
														, columnMenu.title.is("Menu").width.is(layoutless.width().property).noVerticalBorder.is(true).noHorizontalBorder.is(true) //
												})//
						)//
						.width().is(layoutless.width().property)//
						.height().is(layoutless.height().property)//
				);
		/*layoutless.child(new Knob(this)//
				.labelText.is("export")//
				.afterTap.is(new Task() {
					@Override
					public void doTask() {
						orders.exportCurrentDataCSV(ActivityShowOrders.this, "test.csv", "windows-1251");
					}
				}).width().is(100)//
						.height().is(100)//
				);*/
	}
	@Override
	protected void onPause() {
		Tools.db(this).close();
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		Tools.db(this).close();
		super.onDestroy();
	}
}
